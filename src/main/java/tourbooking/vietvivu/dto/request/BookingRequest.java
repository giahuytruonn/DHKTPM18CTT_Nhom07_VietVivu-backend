package tourbooking.vietvivu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private String tourId;
    private String userId;
    private int numberOfGuests;
}
