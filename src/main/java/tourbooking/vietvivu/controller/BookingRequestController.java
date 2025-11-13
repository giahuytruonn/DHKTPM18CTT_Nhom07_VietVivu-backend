package tourbooking.vietvivu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.BookingCancelUpdateRequest;
import tourbooking.vietvivu.dto.request.BookingRequestStatusUpdateRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.BookingRequestResponse;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.UserRepository;
import tourbooking.vietvivu.service.BookingRequestService;

@RestController
@RequestMapping("/bookings-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingRequestController {

    final BookingRequestService bookingRequestService;
    final UserRepository userRepository;

    @GetMapping
    ApiResponse<List<BookingRequestResponse>> getPendingRequests() {
        return ApiResponse.<List<BookingRequestResponse>>builder()
                .result(bookingRequestService.getPendingRequests())
                .build();
    }

    @GetMapping("/{requestId}")
    ApiResponse<BookingRequestResponse> getBookingRequest(@PathVariable String requestId) {
        return ApiResponse.<BookingRequestResponse>builder()
                .result(bookingRequestService.getById(requestId))
                .build();
    }

    @PutMapping("/{requestId}/status")
    ApiResponse<BookingRequestResponse> updateStatusBooking(
            @PathVariable String requestId, @RequestBody @Valid BookingRequestStatusUpdateRequest request) {

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User admin =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String adminId = admin.getId();

        log.info(
                "Updating booking request status: requestId={}, status={}, adminId={}",
                requestId,
                request.getStatus(),
                adminId);

        return ApiResponse.<BookingRequestResponse>builder()
                .result(bookingRequestService.updateBookingRequestStatusAdmin(requestId, adminId, request))
                .build();
    }

    @PutMapping("/{bookingId}/cancel-booking")
    ApiResponse<BookingRequestResponse> cancelBooking(
            @PathVariable String bookingId, @RequestBody @Valid BookingCancelUpdateRequest request) {

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String userId = user.getId();

        log.info(
                "Canceling booking request: bookingId={}, reason={}, userId={}",
                bookingId,
                request.getReason(),
                userId);

        return ApiResponse.<BookingRequestResponse>builder()
                .result(bookingRequestService.updateBookingRequestStatusCustomer(bookingId, userId, request))
                .build();
    }
}
