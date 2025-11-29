package tourbooking.vietvivu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.service.FavoriteTourService;

@RestController
@RequestMapping("/users/favorite-tours")
@RequiredArgsConstructor
@Slf4j
public class FavoriteTourController {

    private final FavoriteTourService favoriteTourService;

    @PostMapping("/{tourId}")
    public ApiResponse<Void> addToFavorites(@PathVariable String tourId) {
        log.info("POST /users/favorite-tours/{} - Adding tour to favorites", tourId);
        try {
            favoriteTourService.addToFavorites(tourId);
            log.info("Successfully added tour {} to favorites", tourId);
            return ApiResponse.<Void>builder()
                    .message("Tour added to favorites")
                    .build();
        } catch (Exception e) {
            log.error("Error adding tour {} to favorites: {}", tourId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/{tourId}")
    public ApiResponse<Void> removeFromFavorites(@PathVariable String tourId) {
        log.info("DELETE /users/favorite-tours/{} - Removing tour from favorites", tourId);
        try {
            favoriteTourService.removeFromFavorites(tourId);
            log.info("Successfully removed tour {} from favorites", tourId);
            return ApiResponse.<Void>builder()
                    .message("Tour removed from favorites")
                    .build();
        } catch (Exception e) {
            log.error("Error removing tour {} from favorites: {}", tourId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping
    public ApiResponse<List<TourResponse>> getMyFavorites() {
        log.info("GET /users/favorite-tours - Getting my favorite tours");
        try {
            List<TourResponse> result = favoriteTourService.getMyFavoriteTours();
            log.info("Successfully retrieved {} favorite tours", result.size());
            return ApiResponse.<List<TourResponse>>builder().result(result).build();
        } catch (Exception e) {
            log.error("Error getting favorite tours: {}", e.getMessage(), e);
            return ApiResponse.<List<TourResponse>>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }
}
