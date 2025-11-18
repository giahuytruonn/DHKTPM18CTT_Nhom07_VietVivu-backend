package tourbooking.vietvivu.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    String reviewId;
    Integer rating;
    String comment;
    LocalDate timestamp;
    String userId;
    String userName;
    String tourId;
    String tourTitle;
    String bookingId;
}
