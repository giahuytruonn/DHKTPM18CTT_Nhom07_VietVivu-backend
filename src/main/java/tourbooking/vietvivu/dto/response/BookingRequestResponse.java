package tourbooking.vietvivu.dto.response;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestResponse {

    String requestId;
    String reason;
    ActionType requestType;
    BookingStatus status;
    LocalDateTime reviewedAt;
    LocalDateTime createdAt;
    String adminId;
    String bookingId;
    String newTourId;
    String oldTourId;
    String userId;
}
