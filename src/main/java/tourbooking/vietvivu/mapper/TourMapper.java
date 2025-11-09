package tourbooking.vietvivu.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;

@Mapper(componentModel = "spring")
public interface TourMapper {

    Tour toTour(TourCreateRequest request);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    void updateTour(@MappingTarget Tour tour, TourUpdateRequest request);

    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(tour))")
    @Mapping(target = "totalBookings", expression = "java(mapTotalBookings(tour))")
    TourResponse toTourResponse(Tour tour);

    // === LIST ===
    List<TourResponse> toTourResponseList(List<Tour> tours);

    // Helper methods for complex mapping
    default List<String> mapImageUrls(Tour tour) {
        if (tour.getImages() == null) return List.of();
        return tour.getImages().stream().map(image -> image.getImageUrl()).toList();
    }

    default Integer mapTotalBookings(Tour tour) {
        return tour.getBookings() != null ? tour.getBookings().size() : 0;
    }
}
