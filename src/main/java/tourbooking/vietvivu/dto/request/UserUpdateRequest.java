package tourbooking.vietvivu.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.validator.DobConstraint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String name;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    List<String> roles;
}
