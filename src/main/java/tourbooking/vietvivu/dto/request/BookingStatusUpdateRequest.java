package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BookingStatusUpdateRequest {

    String newTourId;
    String promotionId;

    @NotNull
    String reason;
}
