package tourbooking.vietvivu.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourResponse {
    String tourId;
    String title;
    String description;
    Integer quantity;
    Double priceAdult;
    Double priceChild;
    String duration;
    String destination;
    Boolean availability;
    LocalDate startDate;
    List<String> itinerary;
    List<String> imageUrls;
    Integer totalBookings;
}
