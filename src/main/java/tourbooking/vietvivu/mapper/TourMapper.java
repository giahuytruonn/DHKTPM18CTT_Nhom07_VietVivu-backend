package tourbooking.vietvivu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.entity.Tour;

@Mapper(componentModel = "spring")
public interface TourMapper {

    Tour toTour(TourCreateRequest request);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    void updateTour(@MappingTarget Tour tour, TourUpdateRequest request);
}