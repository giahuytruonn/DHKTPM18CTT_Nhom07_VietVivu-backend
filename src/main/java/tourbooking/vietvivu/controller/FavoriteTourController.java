package tourbooking.vietvivu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.service.FavoriteTourService;

import java.util.List;

@RestController
@RequestMapping("/users/favorite-tours")
@RequiredArgsConstructor
public class FavoriteTourController {

    private final FavoriteTourService favoriteTourService;

    @PostMapping("/{tourId}")
    public ApiResponse<Void> addToFavorites(@PathVariable String tourId) {
        favoriteTourService.addToFavorites(tourId);
        return ApiResponse.<Void>builder()
                .message("Tour added to favorites")
                .build();
    }

    @DeleteMapping("/{tourId}")
    public ApiResponse<Void> removeFromFavorites(@PathVariable String tourId) {
        favoriteTourService.removeFromFavorites(tourId);
        return ApiResponse.<Void>builder()
                .message("Tour removed from favorites")
                .build();
    }

    @GetMapping
    public ApiResponse<List<TourResponse>> getMyFavorites() {
        return ApiResponse.<List<TourResponse>>builder()
                .result(favoriteTourService.getMyFavoriteTours())
                .build();
    }
}