package tourbooking.vietvivu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import tourbooking.vietvivu.dto.request.RoleRequest;
import tourbooking.vietvivu.dto.response.RoleResponse;
import tourbooking.vietvivu.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
