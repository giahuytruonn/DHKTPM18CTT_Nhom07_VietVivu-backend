package tourbooking.vietvivu.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.service.TourService;

import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourController {

    TourService tourService;

    //public

    @GetMapping("/search")
    public ApiResponse<List<TourResponse>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<TourResponse> result = tourService.searchTours(keyword, destination, minPrice, maxPrice);
        return ApiResponse.<List<TourResponse>>builder()
                .result(result)
                .build();
    }

    //admin


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourResponse> createTour(@RequestBody @Valid TourCreateRequest request) {
        TourResponse result = tourService.createTour(request);
        return ApiResponse.<TourResponse>builder()
                .result(result)
                .build();
    }

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

    @DeleteMapping("/{tourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteTour(@PathVariable String tourId) {
        tourService.deleteTour(tourId);
        return ApiResponse.<Void>builder()
                .message("Tour deleted successfully")
                .build();
    }


//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<List<TourResponse>> getAllTours() {
//        List<TourResponse> result = tourService.getAllTours();
//        return ApiResponse.<List<TourResponse>>builder()
//                .result(result)
//                .build();
//    }
//
//    @GetMapping("/{tourId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<TourResponse> getTour(@PathVariable String tourId) {
//        TourResponse result = tourService.getTour(tourId);
//        return ApiResponse.<TourResponse>builder()
//                .result(result)
//                .build();
//    }

    @GetMapping
    public ApiResponse<List<TourResponse>> getAllToursPublic() {
        List<TourResponse> result = tourService.getAllTours();
        return ApiResponse.<List<TourResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/{tourId}")
    public ApiResponse<TourResponse> getTourPublic(@PathVariable String tourId) {
        TourResponse result = tourService.getTour(tourId);
        return ApiResponse.<TourResponse>builder()
                .result(result)
                .build();
    }
}
