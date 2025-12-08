package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FavoriteTourRequest {

    @NotBlank(message = "Tour ID is required")
    @JsonProperty("tourId")
    String tourId;
}
