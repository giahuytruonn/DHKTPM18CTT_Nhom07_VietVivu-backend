package tourbooking.vietvivu.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.PermissionRequest;
import tourbooking.vietvivu.dto.response.PermissionResponse;
import tourbooking.vietvivu.entity.Permission;
import tourbooking.vietvivu.mapper.PermissionMapper;
import tourbooking.vietvivu.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAllPermission() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    public void delete(String name) {
        permissionRepository.deleteById(name);
    }
}
