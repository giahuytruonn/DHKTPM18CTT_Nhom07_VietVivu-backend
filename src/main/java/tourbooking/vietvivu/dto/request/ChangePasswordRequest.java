package tourbooking.vietvivu.dto.request;

import jakarta.persistence.Access;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.validator.PasswordConstraint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    String oldPassword;

    @PasswordConstraint
    String newPassword;
}
