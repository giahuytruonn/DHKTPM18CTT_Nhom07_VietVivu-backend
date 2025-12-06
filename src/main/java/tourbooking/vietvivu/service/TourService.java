package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    ImageRepository imageRepository;
    TourMapper tourMapper;
    CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    public List<Tour> findAllTours() {
        return tourRepository.findAll();
    }

    public Tour findTourById(String tourId) {
        return tourRepository.findByTourId(tourId);
    }

    public Map<String, Object> findTourSummaryById(String tourId) {
        return tourRepository.findTourSummaryById(tourId);
    }

    public List<TourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll();
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    public List<TourResponse> getAvailableTours() {
        List<Tour> tours = tourRepository.findAll().stream()
                .filter(tour -> tour.getAvailability() != null && tour.getAvailability())
                .collect(Collectors.toList());
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public TourResponse getTourById(String tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return mapToTourResponse(tour);
    }

    public List<TourResponse> searchTours(String keyword) {
        List<Tour> tours = tourRepository.findByTitleContainingIgnoreCase(keyword);
        if (tours.isEmpty()) {
            tours = tourRepository.findByDestinationContainingIgnoreCase(keyword);
        }
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    private TourResponse mapToTourResponse(Tour tour) {
        // Calculate average rating
        Double averageRating = null;
        Integer reviewCount = 0;
        if (tour.getReviews() != null && !tour.getReviews().isEmpty()) {
            reviewCount = tour.getReviews().size();
            averageRating = tour.getReviews().stream()
                    .filter(review -> review.getRating() != null)
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
        }

        // Get image URLs
        List<String> imageUrls = new ArrayList<>();
        if (tour.getImages() != null && !tour.getImages().isEmpty()) {
            imageUrls = tour.getImages().stream()
                    .map(image -> image.getImageUrl())
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
        }

        // Check if tour is favorite for current user
        Boolean isFavorite = false;
        try {
            var context = SecurityContextHolder.getContext();
            if (context.getAuthentication() != null
                    && context.getAuthentication().isAuthenticated()) {
                String username = context.getAuthentication().getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && user.getFavouriteTours() != null) {
                    isFavorite = user.getFavouriteTours().contains(tour.getTourId());
                }
            }
        } catch (Exception e) {
            // User not authenticated, isFavorite remains false
            log.debug("User not authenticated, setting isFavorite to false");
        }

        return TourResponse.builder()
                .tourId(tour.getTourId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .quantity(tour.getQuantity())
                .priceAdult(tour.getPriceAdult())
                .priceChild(tour.getPriceChild())
                .duration(tour.getDuration())
                .destination(tour.getDestination())
                .availability(tour.getAvailability())
                .startDate(tour.getStartDate())
                .itinerary(tour.getItinerary())
                .imageUrls(imageUrls)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .isFavorite(isFavorite)
                .build();
    }

    /**
     * Get all tours for PUBLIC (User & Guest)
     * Chỉ trả về tours có availability = true và tourStatus = OPEN_BOOKING
     */
    @PreAuthorize("permitAll()")
    public List<TourResponse> getAllToursForPublic() {
        log.info("Getting all tours for public");
        List<Tour> tours = tourRepository.findAll();

        // Update status for all tours
        tours.forEach(this::updateTourStatus);

        // Filter OPEN_BOOKING tours
        List<Tour> openTours = tours.stream()
                .filter(tour -> tour.getAvailability() && tour.getTourStatus() == TourStatus.OPEN_BOOKING)
                .collect(Collectors.toList());

        log.info("Found {} OPEN_BOOKING tours out of {} total tours", openTours.size(), tours.size());
        return tourMapper.toTourResponseList(openTours);
    }

    /**
     * Get ALL tours for ADMIN
     * Trả về tất cả tours bất kể trạng thái
     * FIX: Đảm bảo update status và không bị lỗi khi map
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional() // vẫn cần transaction để gọi @Modifying
    public List<TourResponse> getAllToursForAdmin() {
        log.info("Getting all tours for admin");

        // Bước 1: Cập nhật trạng thái toàn bộ tour bằng 1 câu SQL duy nhất
        // → Không load entity, không saveAll → không bị validate @FutureOrPresent
        tourRepository.updateAllTourStatuses();

        // Bước 2: Lấy danh sách tour mới nhất (status đã được update trong DB)
        List<Tour> tours = tourRepository.findAll();

        log.info("Retrieved {} tours from database with updated status", tours.size());

        // Bước 3: Map sang response (không cần save gì nữa)
        List<TourResponse> responses = tours.stream()
                .map(tour -> {
                    try {
                        return tourMapper.toTourResponse(tour);
                    } catch (Exception e) {
                        log.error("Error mapping tour {}: {}", tour.getTourId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Successfully returned {} tours for admin", responses.size());
        return responses;
    }

    /**
     * Search tours with filters
     */
    @PreAuthorize("permitAll()")
    public List<TourResponse> searchTours(TourSearchRequest request) {
        boolean isAdmin = isAdmin();
        log.info("Searching tours - isAdmin: {}, request: {}", isAdmin, request);

        List<Tour> tours;
        if (isAdmin) {
            tours = tourRepository.searchToursAdmin(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity(),
                    request.getTourStatus());
        } else {
            tours = tourRepository.searchToursPublic(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity());
        }

        log.info("Found {} tours matching search criteria", tours.size());

        // Update tour status
        tours.forEach(this::updateTourStatus);

        // Filter by durationDays if specified
        // Giữ lại logic filter của nhánh Chuc
        if (request.getDurationDays() != null) {
            tours = tours.stream()
                    .filter(tour -> {
                        if (tour.getStartDate() == null || tour.getEndDate() == null) {
                            return false;
                        }
                        long days = ChronoUnit.DAYS.between(tour.getStartDate(), tour.getEndDate());
                        // Sửa logic: "3 ngày 2 đêm" (duration 3) = 2 ngày chênh lệch.
                        // Nếu durationDays = 3, thì (days + 1) == 3
                        return (days + 1) == request.getDurationDays();
                    })
                    .collect(Collectors.toList());
        }

        // Xóa code cũ của nhánh 'main' (public List<TourResponse> searchTours(String...))
        return tourMapper.toTourResponseList(tours);
    }

    /**
     * Get tour by ID
     */
    @PreAuthorize("permitAll()")
    public TourResponse getTour(String tourId) {
        log.info("Getting tour by id: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        updateTourStatus(tour);
        tourRepository.save(tour);
        return tourMapper.toTourResponse(tour);
    }

    // ===== ADMIN OPERATIONS =====

    /**
     * Create new tour - ADMIN only
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TourResponse createTour(TourCreateRequest request) {
        log.info("Creating new tour: {}", request.getTitle());
        Tour tour = tourMapper.toTour(request);

        if (tour.getInitialQuantity() == null) {
            tour.setInitialQuantity(request.getInitialQuantity());
        }

        tour.setQuantity(request.getInitialQuantity());
        tour.setAvailability(true);

        if (tour.getEndDate() == null && request.getStartDate() != null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            tour.setEndDate(request.getStartDate().plusDays(durationDays - 1));
        }

        if (tour.getEndDate() != null && tour.getEndDate().isBefore(tour.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        updateTourStatus(tour);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            Set<Image> images = request.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .imageUrl(url)
                            .uploadDate(LocalDate.now())
                            .tour(tour)
                            .build())
                    .collect(Collectors.toSet());
            tour.setImages(images);
        }

        tourRepository.save(tour);

        log.info("Tour created successfully with id: {}", tour.getTourId());
        return tourMapper.toTourResponse(tour);
    }

    /**
     * Update tour - ADMIN only
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        // Giữ lại logging của nhánh Chuc
        log.info("Updating tour: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (request.getImageUrls() != null) {
            List<String> oldImageUrls =
                    tour.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList());

            cloudinaryService.deleteMultipleImages(oldImageUrls);
            tour.getImages().clear();

            Set<Image> newImages = request.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .imageUrl(url)
                            .uploadDate(LocalDate.now())
                            .tour(tour)
                            .build())
                    .collect(Collectors.toSet());

            tour.getImages().addAll(newImages);
        }

        tourMapper.updateTour(tour, request);
        updateTourStatus(tour);
        tourRepository.save(tour);

        return tourMapper.toTourResponse(tour);
    }

    /**
     * Delete tour - ADMIN only
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTour(String tourId) {
        log.info("Deleting tour: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (tour.getImages() != null && !tour.getImages().isEmpty()) {
            List<String> imageUrls =
                    tour.getImages().stream().map(Image::getImageUrl).toList();
            cloudinaryService.deleteMultipleImages(imageUrls);
        }

        Set<User> users = tour.getUsersFavorited();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                user.getFavoriteTours().remove(tour);
            }
        }
        tour.getUsersFavorited().clear();

        tourRepository.deleteById(tourId);
        log.info("Tour deleted successfully: {}", tourId);
    }

    // ===== PRIVATE HELPER METHODS =====
    // Xóa method getTour bị lặp lại từ nhánh main

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
                endDate = startDate.plusDays(durationDays - 1);
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

        if (now.isBefore(startDate)) {
            if (!tour.getAvailability()) {
                tour.setTourStatus(TourStatus.IN_PROGRESS);
            } else {
                tour.setTourStatus(TourStatus.OPEN_BOOKING);
            }
        } else if (!now.isAfter(endDate)) {
            tour.setTourStatus(TourStatus.IN_PROGRESS);
        } else {
            tour.setTourStatus(TourStatus.COMPLETED);
        }
    }

    private int extractDaysFromDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            log.warn("Duration is null or empty");
            return 1;
        }

        try {
            Pattern pattern1 = Pattern.compile("(\\d+)\\s*ngày", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(duration);
            if (matcher1.find()) {
                return Integer.parseInt(matcher1.group(1));
            }

            Pattern pattern2 = Pattern.compile("(\\d+)\\s*N", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(duration);
            if (matcher2.find()) {
                return Integer.parseInt(matcher2.group(1));
            }

            String trimmed = duration.trim().split("\\s+")[0];
            return Integer.parseInt(trimmed);

        } catch (Exception e) {
            log.warn("Cannot parse duration: {}, error: {}", duration, e.getMessage());
            return 1;
        }
    }

    private boolean isAdmin() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        } catch (Exception e) {
            log.error("Error checking admin role", e);
            return false;
        }
    }
    // Check if tour is favorite for current user
    Boolean isFavorite = false;
}
