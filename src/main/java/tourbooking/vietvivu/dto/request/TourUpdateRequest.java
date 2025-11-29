package tourbooking.vietvivu.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourUpdateRequest {
    String title;
    String description;
    Integer initialQuantity;
    Integer quantity;
    Double priceAdult;
    Double priceChild;
    String duration;
    String destination;
    LocalDate startDate;
    LocalDate endDate;
    List<String> itinerary;
    List<String> imageUrls;
}
