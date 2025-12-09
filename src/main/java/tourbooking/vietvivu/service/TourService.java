package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourScheduleChangeNotification;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.dto.response.TourSelectionResponse;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.TourRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    TourMapper tourMapper;
    CloudinaryService cloudinaryService;
    EmailService emailService;
    BookingRepository bookingRepository;

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

        // LƯU TOUR TRƯỚC (không có ảnh)
        Tour savedTour = tourRepository.save(tour);
        log.info("Tour created with id: {} - processing images asynchronously", savedTour.getTourId());

        // XỬ LÝ ẢNH BẤT ĐỒNG BỘ
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            processImagesAsync(savedTour.getTourId(), request.getImageUrls());
        }

        return tourMapper.toTourResponse(savedTour);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        log.info("Updating tour: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        LocalDate oldStartDate = tour.getStartDate();
        LocalDate oldEndDate = tour.getEndDate();

        boolean datesChanged = false;
        if (request.getStartDate() != null && !request.getStartDate().equals(oldStartDate)) {
            datesChanged = true;
        }
        if (request.getEndDate() != null && !request.getEndDate().equals(oldEndDate)) {
            datesChanged = true;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : tour.getStartDate();

        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        LocalDate lockDate = startDate.minusDays(1);
        boolean isBeforeLockDate = now.isBefore(lockDate.plusDays(1));

        // LƯU DANH SÁCH ẢNH CŨ ĐỂ XÓA ĐỒNG BỘ
        List<String> oldImageUrls = null;
        if (request.getImageUrls() != null && tour.getImages() != null) {
            oldImageUrls = tour.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList());

            // XÓA TỪNG PHẦN TỬ thay vì clear() để tránh lỗi orphan removal
            tour.getImages().removeIf(img -> true);
        }

        // XỬ LÝ TRẠNG THÁI
        if (request.getTourStatus() != null && isBeforeLockDate) {
            if (request.getTourStatus() == TourStatus.OPEN_BOOKING
                    || request.getTourStatus() == TourStatus.IN_PROGRESS) {
                tour.setTourStatus(request.getTourStatus());
                log.info("Admin manually set status to {}", request.getTourStatus());
            }
        }

        // CẬP NHẬT CÁC FIELD KHÁC
        tourMapper.updateTour(tour, request);
        updateTourStatus(tour);

        // LƯU TOUR TRƯỚC
        Tour savedTour = tourRepository.save(tour);
        log.info("Tour {} updated - processing images asynchronously", tourId);

        // XÓA ẢNH CŨ ĐỒNG BỘ VÀ THÊM ẢNH MỚI BẤT ĐỒNG BỘ
        if (request.getImageUrls() != null) {
            List<String> finalOldImageUrls = oldImageUrls;
            updateImagesSync(savedTour.getTourId(), finalOldImageUrls, request.getImageUrls());
        }

        // GỬI EMAIL BẤT ĐỒNG BỘ NẾU THAY ĐỔI NGÀY
        if (datesChanged) {
            log.info("Tour dates changed, sending notifications asynchronously");
            sendScheduleChangeNotificationsAsync(savedTour, oldStartDate, oldEndDate);
        }

        return tourMapper.toTourResponse(savedTour);
    }

    // Thêm method mới với @Async

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTour(String tourId) {
        log.info("Deleting tour: {}", tourId);
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Lưu danh sách URL ảnh để xóa đồng bộ
        List<String> imageUrls = tour.getImages() != null && !tour.getImages().isEmpty()
                ? tour.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList())
                : List.of();

        // Xóa favorite relationships
        Set<User> users = tour.getUsersFavorited();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                user.getFavoriteTours().remove(tour);
            }
        }
        tour.getUsersFavorited().clear();

        // Xóa ảnh ĐỒNG BỘ (xóa luôn)
        if (!imageUrls.isEmpty()) {
            log.info("Deleting {} images synchronously for tour {}", imageUrls.size(), tourId);
            cloudinaryService.deleteMultipleImages(imageUrls);
        }

        // Xóa tour khỏi database
        tourRepository.deleteById(tourId);
        log.info("Tour {} deleted successfully", tourId);
    }

    @Async
    public void deleteImagesAsync(List<String> imageUrls) {
        try {
            log.info("Deleting {} images asynchronously", imageUrls.size());
            cloudinaryService.deleteMultipleImagesAsync(imageUrls).join();
            log.info("Successfully deleted {} images", imageUrls.size());
        } catch (Exception e) {
            log.error("Failed to delete images asynchronously: {}", e.getMessage(), e);
        }
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

        // Tự động tính endDate nếu chưa có
        if (endDate == null && tour.getDuration() != null) {
            int days = extractDaysFromDuration(tour.getDuration());
            if (days > 0) {
                endDate = startDate.plusDays(days - 1);
                tour.setEndDate(endDate);
            }
        }

        // **THÊM LOGIC NÀY: Kiểm tra quantity = 0**
        if (tour.getQuantity() != null && tour.getQuantity() == 0) {
            tour.setAvailability(false);

            // Nếu sau endDate -> COMPLETED
            if (endDate != null && now.isAfter(endDate)) {
                tour.setTourStatus(TourStatus.COMPLETED);
                log.info("Tour {} → COMPLETED (quantity=0, past endDate: {})", tour.getTourId(), endDate);
                return;
            }

            // Nếu từ lockDate (startDate - 1) trở đi -> IN_PROGRESS
            if (!now.isBefore(lockDate.plusDays(1))) {
                tour.setTourStatus(TourStatus.IN_PROGRESS);
                log.info("Tour {} → IN_PROGRESS (quantity=0, on/after lockDate: {})", tour.getTourId(), lockDate);
                return;
            }

            // Nếu trước lockDate và quantity = 0 -> vẫn IN_PROGRESS
            tour.setTourStatus(TourStatus.IN_PROGRESS);
            log.info("Tour {} → IN_PROGRESS (quantity=0, before lockDate: {})", tour.getTourId(), lockDate);
            return;
        }

        // Ưu tiên cao nhất: Sau endDate → COMPLETED
        if (endDate != null && now.isAfter(endDate)) {
            tour.setTourStatus(TourStatus.COMPLETED);
            log.info("Tour {} → COMPLETED (past endDate: {})", tour.getTourId(), endDate);
            return;
        }

        // Từ ngày lockDate (tức startDate - 1) trở đi → IN_PROGRESS
        if (!now.isBefore(lockDate.plusDays(1))) {
            tour.setTourStatus(TourStatus.IN_PROGRESS);
            log.info("Tour {} → IN_PROGRESS (on/after lockDate: {})", tour.getTourId(), lockDate);
            return;
        }

        // Trước lockDate → giữ trạng thái admin đã set, hoặc mặc định
        if (tour.getTourStatus() == null || tour.getTourStatus() == TourStatus.COMPLETED) {
            tour.setTourStatus(tour.getAvailability() ? TourStatus.OPEN_BOOKING : TourStatus.IN_PROGRESS);
        }

        log.info("Tour {} remains {} (still before lockDate: {})", tour.getTourId(), tour.getTourStatus(), lockDate);
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

    private void sendScheduleChangeNotifications(Tour tour, LocalDate oldStartDate, LocalDate oldEndDate) {
        try {
            // LẤY TẤT CẢ BOOKING CÓ STATUS PENDING HOẶC CONFIRMED
            List<Booking> bookings = new ArrayList<>();
            bookings.addAll(
                    bookingRepository.findByTourTourIdAndBookingStatus(tour.getTourId(), BookingStatus.PENDING));
            bookings.addAll(
                    bookingRepository.findByTourTourIdAndBookingStatus(tour.getTourId(), BookingStatus.CONFIRMED));

            log.info("Found {} bookings to notify for tour {}", bookings.size(), tour.getTourId());

            for (Booking booking : bookings) {
                try {
                    String customerName;
                    String customerEmail;

                    // LẤY THÔNG TIN KHÁCH HÀNG TỪ USER HOẶC CONTACT
                    if (booking.getUser() != null) {
                        customerName = booking.getUser().getName();
                        customerEmail = booking.getUser().getEmail();
                    } else if (booking.getContact() != null) {
                        customerName = booking.getContact().getName();
                        customerEmail = booking.getContact().getEmail();
                    } else {
                        log.warn("Booking {} has no user or contact, skipping notification", booking.getBookingId());
                        continue;
                    }

                    // TẠO NOTIFICATION OBJECT
                    TourScheduleChangeNotification notification = TourScheduleChangeNotification.builder()
                            .tourId(tour.getTourId())
                            .tourTitle(tour.getTitle())
                            .tourDestination(tour.getDestination())
                            .oldStartDate(oldStartDate)
                            .oldEndDate(oldEndDate)
                            .newStartDate(tour.getStartDate())
                            .newEndDate(tour.getEndDate())
                            .customerName(customerName)
                            .customerEmail(customerEmail)
                            .build();

                    // GỬI EMAIL
                    emailService.sendTourScheduleChangeEmail(notification);
                    log.info(
                            "Schedule change notification sent to {} for booking {}",
                            customerEmail,
                            booking.getBookingId());

                } catch (Exception e) {
                    log.error("Failed to send notification for booking {}: {}", booking.getBookingId(), e.getMessage());
                }
            }

            log.info(
                    "Completed sending {} schedule change notifications for tour {}",
                    bookings.size(),
                    tour.getTourId());

        } catch (Exception e) {
            log.error(
                    "Error while sending schedule change notifications for tour {}: {}",
                    tour.getTourId(),
                    e.getMessage());
        }
    }

    public List<TourSelectionResponse> getAllTourNames() {
        return tourRepository.findAllTourNames();
    }

    @Async
    protected void sendScheduleChangeNotificationsAsync(Tour tour, LocalDate oldStartDate, LocalDate oldEndDate) {
        sendScheduleChangeNotifications(tour, oldStartDate, oldEndDate);
    }

    @Async
    public void processImagesAsync(String tourId, List<String> imageUrls) {
        try {
            log.info("Processing {} images for tour {}", imageUrls.size(), tourId);

            Tour tour = tourRepository.findById(tourId).orElse(null);
            if (tour == null) {
                log.error("Tour {} not found for async image processing", tourId);
                return;
            }

            // Khởi tạo collection nếu null
            if (tour.getImages() == null) {
                tour.setImages(new HashSet<>());
            }

            // Thêm ảnh mới vào collection hiện tại
            imageUrls.forEach(url -> {
                Image image = Image.builder()
                        .imageUrl(url)
                        .uploadDate(LocalDate.now())
                        .tour(tour)
                        .build();
                tour.getImages().add(image);
            });

            tourRepository.save(tour);

            log.info("Successfully added {} images to tour {}", imageUrls.size(), tourId);
        } catch (Exception e) {
            log.error("Failed to process images for tour {}: {}", tourId, e.getMessage(), e);
        }
    }

    public void updateImagesSync(String tourId, List<String> oldImageUrls, List<String> newImageUrls) {
        try {
            // Xóa ảnh cũ ĐỒNG BỘ từ Cloudinary
            if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                log.info("Deleting {} old images synchronously for tour {}", oldImageUrls.size(), tourId);
                cloudinaryService.deleteMultipleImages(oldImageUrls);
            }

            // Thêm ảnh mới BẤT ĐỒNG BỘ vào DB
            if (newImageUrls != null && !newImageUrls.isEmpty()) {
                processImagesAsync(tourId, newImageUrls);
            }

            log.info("Successfully updated images for tour {}", tourId);
        } catch (Exception e) {
            log.error("Failed to update images for tour {}: {}", tourId, e.getMessage(), e);
        }
    }
}
