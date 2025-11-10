package tourbooking.vietvivu.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.repository.TourRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    ImageRepository imageRepository;
    TourMapper tourMapper;

    /**
     * Get all tours for PUBLIC (User & Guest)
     * Chỉ trả về tours có availability = true và tourStatus = OPEN_BOOKING
     */
    public List<TourResponse> getAllToursForPublic() {
        List<Tour> tours = tourRepository.findAll();

        tours.forEach(this::updateTourStatus);

        List<Tour> openTours = tours.stream()
                .filter(tour -> tour.getAvailability() && tour.getTourStatus() == TourStatus.OPEN_BOOKING)
                .collect(Collectors.toList());

        return tourMapper.toTourResponseList(openTours);
    }

    /**
     * Get ALL tours for ADMIN
     * Trả về tất cả tours bất kể trạng thái
     */
    public List<TourResponse> getAllToursForAdmin() {
        List<Tour> tours = tourRepository.findAll();
        tours.forEach(this::updateTourStatus);
        return tourMapper.toTourResponseList(tours);
    }

    /**
     * Search tours with filters
     * - Public: chỉ tìm OPEN_BOOKING tours
     * - Admin: tìm tất cả + filter theo tourStatus
     */
    public List<TourResponse> searchTours(TourSearchRequest request) {
        boolean isAdmin = isAdmin();
        List<Tour> tours;

        if (isAdmin) {
            // ADMIN: Tìm kiếm tất cả tours + có thể filter theo tourStatus
            tours = tourRepository.searchToursAdmin(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity(),
                    request.getTourStatus()  // Admin có thể filter theo status
            );
        } else {
            // USER/GUEST: Chỉ tìm OPEN_BOOKING tours
            tours = tourRepository.searchToursPublic(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity()
                    // KHÔNG có tourStatus - đã fix cứng trong query
            );
        }

        // Update tour status (quan trọng để cập nhật trạng thái realtime)
        tours.forEach(this::updateTourStatus);

        // Filter by durationDays nếu có
        if (request.getDurationDays() != null) {
            tours = tours.stream()
                    .filter(tour -> {
                        if (tour.getStartDate() == null || tour.getEndDate() == null) {
                            return false;
                        }
                        long days = ChronoUnit.DAYS.between(
                                tour.getStartDate(),
                                tour.getEndDate()
                        );
                        return days == request.getDurationDays();
                    })
                    .collect(Collectors.toList());
        }

        return tourMapper.toTourResponseList(tours);
    }

    /**
     * Get tour by ID
     * Bất kỳ ai cũng có thể xem chi tiết tour
     */
    public TourResponse getTour(String tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        updateTourStatus(tour);
        return tourMapper.toTourResponse(tour);
    }

    // ===== ADMIN OPERATIONS =====

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        Tour tour = tourMapper.toTour(request);

        if (tour.getInitialQuantity() == null) {
            tour.setInitialQuantity(request.getInitialQuantity());
        }

        tour.setQuantity(request.getInitialQuantity());
        tour.setAvailability(true);

        if (tour.getEndDate() == null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            tour.setEndDate(request.getStartDate().plusDays(durationDays));
        }

        if (tour.getEndDate().isBefore(tour.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        updateTourStatus(tour);
        tour = tourRepository.save(tour);
        saveImages(tour, request.getImageUrls());
        return tourMapper.toTourResponse(tour);
    }

    @Transactional
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        tourMapper.updateTour(tour, request);

        if (request.getImageUrls() != null) {
            imageRepository.deleteByTour_TourId(tourId);
            saveImages(tour, request.getImageUrls());
        }

        updateTourStatus(tour);
        tour = tourRepository.save(tour);
        return tourMapper.toTourResponse(tour);
    }

    @Transactional
    public void deleteTour(String tourId) {
        if (!tourRepository.existsById(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }
        tourRepository.deleteById(tourId);
    }

    // ===== PRIVATE HELPER METHODS =====

    private void saveImages(Tour tour, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        Set<Image> images = imageUrls.stream()
                .map(url -> Image.builder()
                        .imageUrl(url)
                        .uploadDate(LocalDate.now())
                        .tour(tour)
                        .build())
                .collect(Collectors.toSet());
        imageRepository.saveAll(images);
    }

    private void updateTourStatus(Tour tour) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = tour.getStartDate();
        LocalDate endDate = tour.getEndDate();

        if (startDate == null) {
            log.warn("Tour {} has null startDate, setting status to COMPLETED", tour.getTourId());
            tour.setTourStatus(TourStatus.COMPLETED);
            return;
        }

        if (endDate == null && tour.getDuration() != null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            if (durationDays > 0) {
                endDate = startDate.plusDays(durationDays);
                tour.setEndDate(endDate);
            } else {
                log.warn("Tour {} has invalid duration format: {}", tour.getTourId(), tour.getDuration());
                tour.setTourStatus(TourStatus.COMPLETED);
                return;
            }
        }

        if (endDate == null) {
            log.warn("Tour {} has null endDate after calculation", tour.getTourId());
            tour.setTourStatus(TourStatus.COMPLETED);
            return;
        }

        // Xác định trạng thái tour
        if (now.isBefore(startDate.minusDays(1))) {
            // Trước ngày bắt đầu
            if (!tour.getAvailability()) {
                tour.setTourStatus(TourStatus.IN_PROGRESS);
            } else {
                tour.setTourStatus(TourStatus.OPEN_BOOKING);
            }
        } else if (!now.isAfter(endDate)) {
            // Trong khoảng thời gian tour
            tour.setTourStatus(TourStatus.IN_PROGRESS);
        } else {
            // Sau khi tour kết thúc
            tour.setTourStatus(TourStatus.COMPLETED);
        }
    }

    private int extractDaysFromDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            log.warn("Duration is null or empty");
            return 1;
        }

        try {
            // Pattern 1: "3 ngày", "3 ngày 2 đêm"
            Pattern pattern1 = Pattern.compile("(\\d+)\\s*ngày", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(duration);
            if (matcher1.find()) {
                return Integer.parseInt(matcher1.group(1));
            }

            // Pattern 2: "3N", "3N2Đ"
            Pattern pattern2 = Pattern.compile("(\\d+)\\s*N", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(duration);
            if (matcher2.find()) {
                return Integer.parseInt(matcher2.group(1));
            }

            // Fallback: lấy số đầu tiên
            String trimmed = duration.trim().split("\\s+")[0];
            return Integer.parseInt(trimmed);

        } catch (Exception e) {
            log.warn("Cannot parse duration: {}, error: {}", duration, e.getMessage());
            return 1;
        }
    }

    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}