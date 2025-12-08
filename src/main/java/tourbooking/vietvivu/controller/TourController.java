package tourbooking.vietvivu.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.service.CloudinaryService;
import tourbooking.vietvivu.service.TourService;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourController {

    TourService tourService;
    CloudinaryService cloudinaryService;

    // ===== PUBLIC ENDPOINTS =====

    @GetMapping
    public ApiResponse<PaginationResponse<TourResponse>> getAllToursPublic(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("GET /tours - Getting all public tours with pagination: page={}, size={}", page, size);
        try {
            PaginationResponse<TourResponse> result = tourService.getAllToursForPublic(page, size);
            log.info(
                    "Successfully retrieved {} public tours on page {}",
                    result.getItems().size(),
                    page);
            return ApiResponse.<PaginationResponse<TourResponse>>builder()
                    .result(result)
                    .build();
        } catch (Exception e) {
            log.error("Error getting public tours", e);
            throw e;
        }
    }

    @GetMapping("/{tourId}")
    public ApiResponse<TourResponse> getTourPublic(@PathVariable String tourId) {
        log.info("GET /tours/{} - Getting tour details", tourId);
        try {
            TourResponse result = tourService.getTour(tourId);
            log.info("Successfully retrieved tour {}", tourId);
            return ApiResponse.<TourResponse>builder().result(result).build();
        } catch (Exception e) {
            log.error("Error getting tour {}", tourId, e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<TourResponse>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) TourStatus tourStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(
                "GET /tours/search - keyword: {}, destination: {}, minPrice: {}, maxPrice: {}, tourStatus: {}, page: {}, size: {}",
                keyword,
                destination,
                minPrice,
                maxPrice,
                tourStatus,
                page,
                size);

        try {
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

            PaginationResponse<TourResponse> result = tourService.searchTours(request, page, size);
            log.info("Search found {} tours on page {}", result.getItems().size(), page);
            return ApiResponse.<PaginationResponse<TourResponse>>builder()
                    .result(result)
                    .build();
        } catch (Exception e) {
            log.error("Error searching tours", e);
            throw e;
        }
    }

    // ===== ADMIN ENDPOINTS =====

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaginationResponse<TourResponse>> getAllToursAdmin(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("GET /tours/admin/all - Getting all tours for admin: page={}, size={}", page, size);
        try {
            PaginationResponse<TourResponse> result = tourService.getAllToursForAdmin(page, size);
            log.info(
                    "Successfully retrieved {} tours for admin on page {}",
                    result.getItems().size(),
                    page);
            return ApiResponse.<PaginationResponse<TourResponse>>builder()
                    .result(result)
                    .message("Retrieved " + result.getTotalItems() + " tours")
                    .build();
        } catch (Exception e) {
            log.error("Error getting tours for admin", e);
            return ApiResponse.<PaginationResponse<TourResponse>>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourResponse> createTour(@RequestBody @Valid TourCreateRequest request) {
        log.info("POST /tours - Creating new tour: {}", request.getTitle());
        try {
            TourResponse result = tourService.createTour(request);
            log.info("Successfully created tour with id: {}", result.getTourId());
            return ApiResponse.<TourResponse>builder()
                    .result(result)
                    .message("Tour created successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error creating tour", e);
            throw e;
        }
    }

    @PutMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourResponse> updateTour(
            @PathVariable String tourId, @RequestBody @Valid TourUpdateRequest request) {

        log.info("PUT /tours/{} - Updating tour", tourId);
        try {
            TourResponse result = tourService.updateTour(tourId, request);
            log.info("Successfully updated tour {}", tourId);
            return ApiResponse.<TourResponse>builder()
                    .result(result)
                    .message("Tour updated successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error updating tour {}", tourId, e);
            throw e;
        }
    }

    @DeleteMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteTour(@PathVariable String tourId) {
        log.info("DELETE /tours/{} - Deleting tour", tourId);
        try {
            tourService.deleteTour(tourId);
            log.info("Successfully deleted tour {}", tourId);
            return ApiResponse.<Void>builder()
                    .message("Tour deleted successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error deleting tour {}", tourId, e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping(value = "/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        log.info("Uploading {} images", files.size());
        try {
            List<String> imageUrls = cloudinaryService.uploadMultipleImages(files);
            return ApiResponse.<List<String>>builder()
                    .result(imageUrls)
                    .message("Upload successful")
                    .build();
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ApiResponse.<List<String>>builder()
                    .code(500)
                    .message("Upload failed: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/delete-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteImage(@RequestParam String imageUrl) {
        log.info("Deleting image: {}", imageUrl);
        try {
            cloudinaryService.deleteImage(imageUrl);
            return ApiResponse.<Void>builder()
                    .message("Image deleted successfully")
                    .build();
        } catch (Exception e) {
            log.error("Delete failed", e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Delete failed: " + e.getMessage())
                    .build();
        }
    }
}
