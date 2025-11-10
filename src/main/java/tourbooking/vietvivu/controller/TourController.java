package tourbooking.vietvivu.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.service.TourService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourController {

    TourService tourService;

    // ===== PUBLIC ENDPOINTS (User & Guest) =====

    /**
     * Get all tours that are OPEN_BOOKING (for public)
     * User và Guest chỉ xem được tours đang mở booking
     */
    @GetMapping
    public ApiResponse<List<TourResponse>> getAllToursPublic() {
        List<TourResponse> result = tourService.getAllToursForPublic();
        return ApiResponse.<List<TourResponse>>builder()
                .result(result)
                .build();
    }

    /**
     * Get tour by ID (public can access)
     */
    @GetMapping("/{tourId}")
    public ApiResponse<TourResponse> getTourPublic(@PathVariable String tourId) {
        TourResponse result = tourService.getTour(tourId);
        return ApiResponse.<TourResponse>builder()
                .result(result)
                .build();
    }

    /**
     * Search tours with filters (public)
     */
    @GetMapping("/search")
    public ApiResponse<List<TourResponse>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) TourStatus tourStatus) {

        TourSearchRequest request = TourSearchRequest.builder()
                .keyword(keyword)
                .destination(destination)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .startDate(startDate)
                .durationDays(durationDays)
                .minQuantity(minQuantity)
                .tourStatus(tourStatus)
                .build();

        List<TourResponse> result = tourService.searchTours(request);
        return ApiResponse.<List<TourResponse>>builder()
                .result(result)
                .build();
    }

    // ===== ADMIN ENDPOINTS =====

    /**
     * Get ALL tours (for admin only)
     * Admin có thể xem tất cả tours bất kể trạng thái
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TourResponse>> getAllToursAdmin() {
        List<TourResponse> result = tourService.getAllToursForAdmin();
        return ApiResponse.<List<TourResponse>>builder()
                .result(result)
                .build();
    }

    /**
     * Create new tour (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourResponse> createTour(@RequestBody @Valid TourCreateRequest request) {
        TourResponse result = tourService.createTour(request);
        return ApiResponse.<TourResponse>builder()
                .result(result)
                .build();
    }

    /**
     * Update tour (admin only)
     */
    @PutMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourResponse> updateTour(
            @PathVariable String tourId,
            @RequestBody @Valid TourUpdateRequest request) {

        TourResponse result = tourService.updateTour(tourId, request);
        return ApiResponse.<TourResponse>builder()
                .result(result)
                .build();
    }

    /**
     * Delete tour (admin only)
     */
    @DeleteMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteTour(@PathVariable String tourId) {
        tourService.deleteTour(tourId);
        return ApiResponse.<Void>builder()
                .message("Tour deleted successfully")
                .build();
    }
}