package tourbooking.vietvivu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.core.context.SecurityContextHolder;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "availability", ignore = true)
    Tour toTour(TourCreateRequest request);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    void updateTour(@MappingTarget Tour tour, TourUpdateRequest request);

    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(tour))")
    @Mapping(target = "totalBookings", expression = "java(mapTotalBookings(tour))")
    @Mapping(target = "favoriteCount", expression = "java(mapFavoriteCount(tour))")
    @Mapping(target = "isFavorited", expression = "java(isFavoritedByCurrentUser(tour))")
    TourResponse toTourResponse(Tour tour);

    List<TourResponse> toTourResponseList(List<Tour> tours);

    default List<String> mapImageUrls(Tour tour) {
        if (tour.getImages() == null) return List.of();
        return tour.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
    }

    default Integer mapTotalBookings(Tour tour) {
        return tour.getBookings() != null ? tour.getBookings().size() : 0;
    }

    default Integer mapFavoriteCount(Tour tour) {
        return tour.getUsersFavorited() != null ? tour.getUsersFavorited().size() : 0;
    }

    default Boolean isFavoritedByCurrentUser(Tour tour) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return false;
        }

        String username = authentication.getName();
        return tour.getUsersFavorited().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }
}