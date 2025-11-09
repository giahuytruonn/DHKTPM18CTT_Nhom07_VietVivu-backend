package tourbooking.vietvivu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.TourStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourResponse {
    String tourId;
    String title;
    String description;
    Integer initialQuantity;
    Integer quantity;
    Double priceAdult;
    Double priceChild;
    String duration;
    String destination;
    Boolean availability;
    LocalDate startDate;
    LocalDate endDate;
    TourStatus tourStatus;
    List<String> itinerary;
    List<String> imageUrls;
    Integer totalBookings;
    Integer favoriteCount;
    Boolean isFavorited;
}