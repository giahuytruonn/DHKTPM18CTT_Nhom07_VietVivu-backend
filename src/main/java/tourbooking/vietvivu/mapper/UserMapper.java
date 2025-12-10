package tourbooking.vietvivu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import tourbooking.vietvivu.dto.request.UserCreationRequest;
import tourbooking.vietvivu.dto.request.UserUpdateRequest;
import tourbooking.vietvivu.dto.response.UserResponse;
import tourbooking.vietvivu.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "invalidatedTokens", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
