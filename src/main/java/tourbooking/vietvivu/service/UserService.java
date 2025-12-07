package tourbooking.vietvivu.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import tourbooking.vietvivu.constant.PredefinedRole;
import tourbooking.vietvivu.dto.request.PasswordCreationRequest;
import tourbooking.vietvivu.dto.request.UserCreationRequest;
import tourbooking.vietvivu.dto.request.UserUpdateRequest;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.dto.response.UserResponse;
import tourbooking.vietvivu.entity.Role;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.UserMapper;
import tourbooking.vietvivu.repository.RoleRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // CREATE
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var userResponse = userMapper.toUserResponse(user);

        userResponse.setNoPassword(!StringUtils.hasText(user.getPassword()));

        return userResponse;
    }

    public void createPassword(PasswordCreationRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (StringUtils.hasText(user.getPassword())) throw new AppException(ErrorCode.PASSWORD_EXISTED);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    // UPDATE
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        HashSet<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles.addAll(roleRepository.findAllById(request.getRoles()));
        }

        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // DELETE
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateStatusUser(String id, Boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setIsActive(isActive);
        userRepository.save(user);
    }

    // SEARCH With Name, phone
    public List<UserResponse> searchUsers(String keyword) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrPhoneNumberContaining(keyword, keyword);
        return users.stream().map(userMapper::toUserResponse).toList();
    }

    // READ
//    @PreAuthorize("hasRole('ADMIN')")
//    public List<UserResponse> getUsers() {
//        log.info("In method get Users");
//        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
//    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse <UserResponse> getUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(pageable);

        List<UserResponse> res = users.getContent()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        return PaginationResponse.<UserResponse> builder()
                .items(res)
                .currentPage(users.getNumber())
                .pageSizes(users.getSize())
                .totalItems((int) users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
