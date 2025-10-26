package tourbooking.vietvivu.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    String tourId;
    String userId;
    LocalDate bookingDate;

    //Contact information
    String name;
    String email;
    String phone;
    String address;
    String note;

    int numOfAdults;
    int numOfChildren;

    //promotion
    String promotionId;
}
