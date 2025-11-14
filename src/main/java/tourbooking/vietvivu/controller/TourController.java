package tourbooking.vietvivu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.service.TourService;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourController {
    TourService tourService;

    @GetMapping
    public ApiResponse<List<TourResponse>> getAllTours() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAllTours())
                .build();
    }

    @GetMapping("/available")
    public ApiResponse<List<TourResponse>> getAvailableTours() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.getAvailableTours())
                .build();
    }

    @GetMapping("/{tourId}")
    public ApiResponse<TourResponse> getTourById(@PathVariable String tourId) {
        return ApiResponse.<TourResponse>builder()
                .result(tourService.getTourById(tourId))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<TourResponse>> searchTours(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<TourResponse>>builder()
                .result(tourService.searchTours(keyword))
                .build();
    }
}
