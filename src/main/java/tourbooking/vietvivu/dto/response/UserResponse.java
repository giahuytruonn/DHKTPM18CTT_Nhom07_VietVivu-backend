package tourbooking.vietvivu.dto.response;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String name;
    String email;
    String address;
    String phoneNumber;
    Boolean noPassword;
    Boolean isActive;
    Set<RoleResponse> roles;
}
