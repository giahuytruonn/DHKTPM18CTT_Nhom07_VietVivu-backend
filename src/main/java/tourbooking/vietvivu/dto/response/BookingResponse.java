package tourbooking.vietvivu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;
import tourbooking.vietvivu.enumm.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

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


    //Booking details
    String bookingId;
    LocalDateTime bookingDate;
    Double totalPrice;
    String promotionCode;
    Double discountAmount;
    Double remainingAmount;
    BookingStatus bookingStatus;
    LocalDateTime paymentTerm;

    //Tour details
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
