package tourbooking.vietvivu.dto.request;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourScheduleChangeNotification {
    String tourId;
    String tourTitle;
    String tourDestination;
    LocalDate oldStartDate;
    LocalDate oldEndDate;
    LocalDate newStartDate;
    LocalDate newEndDate;
    String customerName;
    String customerEmail;
}
