package tourbooking.vietvivu.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.RoleRequest;
import tourbooking.vietvivu.dto.response.RoleResponse;
import tourbooking.vietvivu.entity.Role;
import tourbooking.vietvivu.mapper.RoleMapper;
import tourbooking.vietvivu.repository.PermissionRepository;
import tourbooking.vietvivu.repository.RoleRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleMapper roleMapper;
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    public RoleResponse createRole(RoleRequest request) {
        Role role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
