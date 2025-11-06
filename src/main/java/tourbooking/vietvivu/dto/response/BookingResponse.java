package tourbooking.vietvivu.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.BookingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    // Contact information
    String name;
    String email;
    String phone;
    String address;
    String note;

    // Booking details
    String bookingId;
    LocalDateTime bookingDate;
    Double totalPrice;
    String promotionCode;
    Double discountAmount;
    Double remainingAmount;
    BookingStatus bookingStatus;
    LocalDateTime paymentTerm;

    // Tour details
    String tourId;
    String tourTitle;
    String tourDuration;
    String tourDestination;
    String imageUrl;

    int numOfAdults;
    Double priceAdult;
    Double totalPriceAdults;

    int numOfChildren;
    Double priceChild;
    Double totalPriceChildren;
}
