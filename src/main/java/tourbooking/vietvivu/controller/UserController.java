package tourbooking.vietvivu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.PasswordCreationRequest;
import tourbooking.vietvivu.dto.request.UserCreationRequest;
import tourbooking.vietvivu.dto.request.UserUpdateRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.dto.response.UserResponse;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.UserRepository;
import tourbooking.vietvivu.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PaginationResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PaginationResponse<UserResponse>>builder()
                .result(userService.getUsers(page, size))
                .message("Get users successfully")
                .build();
    }


//    @GetMapping
//    ApiResponse<List<UserResponse>> getUsers() {
//        return ApiResponse.<List<UserResponse>>builder()
//                .result(userService.getUsers())
//                .build();
//    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PostMapping("/create-password")
    ApiResponse<Void> createPassword(@RequestBody @Valid PasswordCreationRequest request) {
        userService.createPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password has been created, you could use it to log-in")
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam("keyword") String keyword) {
        var result = userService.searchUsers(keyword);
        return ApiResponse.<List<UserResponse>>builder().result(result).build();
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatusUser(@PathVariable String userId, @RequestParam("isActive") Boolean isActive) {
        userService.updateStatusUser(userId, isActive);
        return ApiResponse.<Void>builder()
                .message("User status has been updated")
                .build();
    }

    @PutMapping("/my-info")
    ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyInfo(request))
                .build();

    }
}
