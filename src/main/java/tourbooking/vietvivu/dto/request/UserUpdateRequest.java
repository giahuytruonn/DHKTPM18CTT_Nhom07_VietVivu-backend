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
    String email;
    String phoneNumber;
    String address;

    List<String> roles;
}
