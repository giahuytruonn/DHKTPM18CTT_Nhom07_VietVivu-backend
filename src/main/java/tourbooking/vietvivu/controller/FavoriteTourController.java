package tourbooking.vietvivu.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.FavoriteTourRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.FavoriteTourResponse;
import tourbooking.vietvivu.service.FavoriteTourService;

@RestController
@RequestMapping("/favorite-tours")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FavoriteTourController {
    FavoriteTourService favoriteTourService;

    @PostMapping
    public ApiResponse<FavoriteTourResponse> addFavoriteTour(@RequestBody @Valid FavoriteTourRequest request) {
        try {
            log.info("Received favorite tour request - tourId: {}", request != null ? request.getTourId() : "null");

            if (request == null
                    || request.getTourId() == null
                    || request.getTourId().isBlank()) {
                log.warn("Invalid request received - request is null or tourId is empty");
            }

            FavoriteTourResponse response = favoriteTourService.addFavoriteTour(request);
            log.info("Favorite tour added successfully: {}", response);
            return ApiResponse.<FavoriteTourResponse>builder()
                    .code(1000)
                    .result(response)
                    .build();
        } catch (Exception e) {
            log.error("Error adding favorite tour: ", e);
            throw e; // Re-throw để GlobalHandlerException xử lý
        }
    }

    @DeleteMapping("/{tourId}")
    public ApiResponse<FavoriteTourResponse> removeFavoriteTour(@PathVariable String tourId) {
        return ApiResponse.<FavoriteTourResponse>builder()
                .result(favoriteTourService.removeFavoriteTour(tourId))
                .build();
    }

    @GetMapping
    public ApiResponse<FavoriteTourResponse> getFavoriteTours() {
        return ApiResponse.<FavoriteTourResponse>builder()
                .result(favoriteTourService.getFavoriteTours())
                .build();
    }

    @GetMapping("/{tourId}/check")
    public ApiResponse<Boolean> isFavoriteTour(@PathVariable String tourId) {
        return ApiResponse.<Boolean>builder()
                .result(favoriteTourService.isFavoriteTour(tourId))
                .build();
    }
}
