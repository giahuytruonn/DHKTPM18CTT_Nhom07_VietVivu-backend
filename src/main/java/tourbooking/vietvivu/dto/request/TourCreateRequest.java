package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourCreateRequest {
    @NotBlank
    String title;

    String description;

    @NotNull
    @Positive
    Integer quantity;

    @NotNull
    @PositiveOrZero
    Double priceAdult;

    @NotNull
    @PositiveOrZero
    Double priceChild;

    @NotBlank
    String duration;

    @NotBlank
    String destination;

    @NotNull
    LocalDate startDate;

    @NotEmpty
    List<String> itinerary;

    List<String> imageUrls;
}
