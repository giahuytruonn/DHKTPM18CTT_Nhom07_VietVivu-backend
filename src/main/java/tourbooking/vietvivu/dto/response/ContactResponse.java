package tourbooking.vietvivu.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactResponse {
    String contactId;
    String email;
    String name;
    String address;
    String phoneNumber;

    // Booking info
    String bookingId;
    String bookingStatus;
    LocalDateTime bookingDate;
    Double totalPrice;
    Integer numAdults;
    Integer numChildren;

    // Tour info
    String tourId;
    String tourTitle;
    String tourDestination;
}
