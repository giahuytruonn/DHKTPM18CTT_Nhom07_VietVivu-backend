package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    TourMapper tourMapper;
    CloudinaryService cloudinaryService;
    UserRepository userRepository;

    /**
     * Get all tours for PUBLIC (User & Guest) with pagination
     */
    @PreAuthorize("permitAll()")
    public PaginationResponse<TourResponse> getAllToursForPublic(int page, int size) {
        log.info("Getting all tours for public with pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<Tour> tourPage = tourRepository.findAllPublicTours(pageable);

        tourPage.getContent().forEach(this::updateTourStatus);

        List<TourResponse> responses =
                tourPage.getContent().stream().map(tourMapper::toTourResponse).toList();

        log.info(
                "Found {} OPEN_BOOKING tours on page {} out of {} total pages",
                responses.size(),
                page,
                tourPage.getTotalPages());

        return PaginationResponse.<TourResponse>builder()
                .items(responses)
                .currentPage(tourPage.getNumber())
                .pageSizes(tourPage.getSize())
                .totalItems((int) tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .build();
    }

    /**
     * Get ALL tours for ADMIN with pagination
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional()
    public PaginationResponse<TourResponse> getAllToursForAdmin(int page, int size) {
        log.info("Getting all tours for admin with pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<Tour> tourPage = tourRepository.findAll(pageable);

        tourPage.getContent().forEach(this::updateTourStatus);

        log.info(
                "Retrieved {} tours from database on page {}",
                tourPage.getContent().size(),
                page);

        List<TourResponse> responses =
                tourPage.getContent().stream().map(tourMapper::toTourResponse).toList();

        log.info("Successfully returned {} tours for admin on page {}", responses.size(), page);

        return PaginationResponse.<TourResponse>builder()
                .items(responses)
                .currentPage(tourPage.getNumber())
                .pageSizes(tourPage.getSize())
                .totalItems((int) tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .build();
    }

    /**
     * Search tours with filters and pagination
     */
    @PreAuthorize("permitAll()")
    public PaginationResponse<TourResponse> searchTours(TourSearchRequest request, int page, int size) {
        boolean isAdmin = isAdmin();
        log.info("Searching tours - isAdmin: {}, request: {}, page: {}, size: {}", isAdmin, request, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<Tour> tourPage;

        if (isAdmin) {
            tourPage = tourRepository.searchToursAdmin(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity(),
                    request.getTourStatus(),
                    pageable);
        } else {
            tourPage = tourRepository.searchToursPublic(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getMinQuantity(),
                    pageable);
        }

        log.info(
                "Found {} tours matching search criteria on page {}",
                tourPage.getContent().size(),
                page);

        // Update tour status and filter by durationDays if specified
        List<Tour> tours = tourPage.getContent();
        tours.forEach(this::updateTourStatus);

        if (request.getDurationDays() != null) {
            tours = tours.stream()
                    .filter(tour -> {
                        if (tour.getStartDate() == null || tour.getEndDate() == null) {
                            return false;
                        }
                        long days = ChronoUnit.DAYS.between(tour.getStartDate(), tour.getEndDate());
                        return (days + 1) == request.getDurationDays();
                    })
                    .collect(Collectors.toList());
        }

        List<TourResponse> responses =
                tours.stream().map(tourMapper::toTourResponse).toList();

        return PaginationResponse.<TourResponse>builder()
                .items(responses)
                .currentPage(tourPage.getNumber())
                .pageSizes(tourPage.getSize())
                .totalItems((int) tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .build();
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

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        log.info("Updating tour: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // ===== 1. TÍNH TOÁN CÁC ĐIỀU KIỆN =====
        LocalDate now = LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : tour.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : tour.getEndDate();

        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        LocalDate lockDate = startDate.minusDays(1);

        boolean isBeforeLockDate = !now.isAfter(lockDate);
        boolean isAfterEndDate = endDate != null && now.isAfter(endDate);

        // ===== 2. XỬ LÝ HÌNH ẢNH =====
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

        // ===== 3. XỬ LÝ TRẠNG THÁI TRƯỚC KHI MAPPER =====
        TourStatus finalStatus = tour.getTourStatus(); // Giữ nguyên trạng thái cũ mặc định
        Boolean finalOverride = tour.getManualStatusOverride();

        if (request.getTourStatus() != null) {
            log.info(
                    "Received tourStatus: {}, manualOverride: {}",
                    request.getTourStatus(),
                    request.getManualStatusOverride());

            // SAU endDate → COMPLETED, không cho sửa
            if (isAfterEndDate) {
                log.warn("Cannot update status after endDate. Auto-set to COMPLETED");
                finalStatus = TourStatus.COMPLETED;
                finalOverride = false;
            }
            // Từ lockDate trở đi → IN_PROGRESS, không cho sửa
            else if (!isBeforeLockDate) {
                log.warn("Cannot update status from {} onwards. Auto-set to IN_PROGRESS", lockDate);
                finalStatus = TourStatus.IN_PROGRESS;
                finalOverride = false;
            }
            // TRƯỚC lockDate → Cho phép sửa OPEN_BOOKING hoặc IN_PROGRESS
            else {
                TourStatus requestedStatus = request.getTourStatus();

                if (requestedStatus == TourStatus.OPEN_BOOKING || requestedStatus == TourStatus.IN_PROGRESS) {
                    finalStatus = requestedStatus;
                    finalOverride = Boolean.TRUE.equals(request.getManualStatusOverride());
                    log.info("Admin set status to: {}, override: {}", finalStatus, finalOverride);
                } else {
                    log.warn("Invalid status {} before lockDate. Keeping current status", requestedStatus);
                }
            }
        }

        // Xử lý checkbox "Tắt override"
        if (Boolean.FALSE.equals(request.getManualStatusOverride()) && isBeforeLockDate) {
            finalOverride = false;
            log.info("Admin disabled manual override");
        }

        // ===== 4. GÁN TRẠNG THÁI VÀO ENTITY =====
        tour.setTourStatus(finalStatus);
        tour.setManualStatusOverride(finalOverride);

        // ===== 5. MAPPER CÁC FIELD KHÁC =====
        tourMapper.updateTour(tour, request);

        // ===== 6. TỰ ĐỘNG CẬP NHẬT NẾU KHÔNG CÓ OVERRIDE =====
        if (!Boolean.TRUE.equals(tour.getManualStatusOverride())) {
            updateTourStatus(tour);
            log.info("Auto-updated status to: {}", tour.getTourStatus());
        }

        tourRepository.save(tour);

        log.info(
                "✅ Tour {} updated - Status: {}, Override: {}, LockDate: {}",
                tourId,
                tour.getTourStatus(),
                tour.getManualStatusOverride(),
                lockDate);

        return tourMapper.toTourResponse(tour);
    }

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

    private void updateTourStatus(Tour tour) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = tour.getStartDate();
        LocalDate endDate = tour.getEndDate();

        if (startDate == null) {
            tour.setTourStatus(TourStatus.COMPLETED);
            return;
        }

        LocalDate lockDate = startDate.minusDays(1);

        // ✅ TỰ ĐỘNG TÍNH endDate nếu chưa có
        if (endDate == null && tour.getDuration() != null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            if (durationDays > 0) {
                endDate = startDate.plusDays(durationDays - 1);
                tour.setEndDate(endDate);
            } else {
                tour.setTourStatus(TourStatus.COMPLETED);
                return;
            }
        }

        if (endDate == null) {
            tour.setTourStatus(TourStatus.COMPLETED);
            return;
        }

        // ✅ ƯU TIÊN 1: Check sau endDate → BẮT BUỘC COMPLETED
        if (now.isAfter(endDate)) {
            tour.setTourStatus(TourStatus.COMPLETED);
            tour.setManualStatusOverride(false);
            log.info("Tour {} completed (after endDate)", tour.getTourId());
            return;
        }

        // ✅ ƯU TIÊN 2: Check sau lockDate → BẮT BUỘC IN_PROGRESS
        if (now.isAfter(lockDate)) {
            tour.setTourStatus(TourStatus.IN_PROGRESS);
            tour.setManualStatusOverride(false);
            log.info("Tour {} in progress (after lockDate {})", tour.getTourId(), lockDate);
            return;
        }

        // ✅ ƯU TIÊN 3: Trước hoặc bằng lockDate
        // Nếu có manual override → giữ nguyên status hiện tại
        if (Boolean.TRUE.equals(tour.getManualStatusOverride())) {
            log.info(
                    "Tour {} has manual override before lockDate, keeping status: {}",
                    tour.getTourId(),
                    tour.getTourStatus());
            return;
        }

        // ✅ Không có override → set mặc định
        if (tour.getTourStatus() == null) {
            tour.setTourStatus(tour.getAvailability() ? TourStatus.OPEN_BOOKING : TourStatus.IN_PROGRESS);
        }
        log.info("Tour {} before lockDate, status: {}", tour.getTourId(), tour.getTourStatus());
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
}
