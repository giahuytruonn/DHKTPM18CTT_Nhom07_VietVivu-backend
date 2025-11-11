package tourbooking.vietvivu.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.TourStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourSearchRequest {

    String keyword;
    String destination;
    Double minPrice;
    Double maxPrice;
    LocalDate startDate;
    Integer durationDays;
    Integer minQuantity;


    TourStatus tourStatus;
    Boolean availability;
}