package tourbooking.vietvivu.mapper;

import org.mapstruct.Mapper;

import tourbooking.vietvivu.dto.request.PermissionRequest;
import tourbooking.vietvivu.dto.response.PermissionResponse;
import tourbooking.vietvivu.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
