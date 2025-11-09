package tourbooking.vietvivu.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.BookingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestStatusUpdateRequest implements Serializable {

    @NotNull
    BookingStatus status;
}
