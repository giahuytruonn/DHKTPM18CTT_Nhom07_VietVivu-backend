package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.Email;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.validator.PasswordConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String username;

    @PasswordConstraint
    String password;

    @Email
    String email;

    String name;
    String address;
    String phoneNumber;
}
