package tourbooking.vietvivu.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.core.context.SecurityContextHolder;

import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;

@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "availability", ignore = true)
    Tour toTour(TourCreateRequest request);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "usersFavorited", ignore = true)
    @Mapping(target = "totalBookings", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    void updateTour(@MappingTarget Tour tour, TourUpdateRequest request);

    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(tour))")
    @Mapping(target = "totalBookings", source = "totalBookings")
    @Mapping(target = "favoriteCount", source = "favoriteCount")
    @Mapping(target = "isFavorited", expression = "java(isFavoritedByCurrentUser(tour))")
    @Mapping(target = "manualStatusOverride", source = "manualStatusOverride")
    TourResponse toTourResponse(Tour tour);

    List<TourResponse> toTourResponseList(List<Tour> tours);

    default List<String> mapImageUrls(Tour tour) {
        try {
            if (tour.getImages() == null || tour.getImages().isEmpty()) return List.of();
            return tour.getImages().stream().map(image -> image.getImageUrl()).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    default Boolean isFavoritedByCurrentUser(Tour tour) {
        try {
            var context = SecurityContextHolder.getContext();
            var authentication = context.getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return false;
            }

            String username = authentication.getName();
            if (tour.getUsersFavorited() == null) return false;

            return tour.getUsersFavorited().stream()
                    .anyMatch(user -> user.getUsername().equals(username));
        } catch (Exception e) {
            return false;
        }
    }
}