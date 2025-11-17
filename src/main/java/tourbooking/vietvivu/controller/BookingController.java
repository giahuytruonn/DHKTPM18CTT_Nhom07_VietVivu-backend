package tourbooking.vietvivu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.BookingResponse;
import tourbooking.vietvivu.service.BookingService;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingController {

    BookingService bookingService;

    @GetMapping
    public ApiResponse<List<BookingResponse>> getMyBookings() {
        return ApiResponse.<List<BookingResponse>>builder()
                .result(bookingService.getMyBookings())
                .build();
    }

    @PostMapping
    ApiResponse<BookingResponse> bookTour(@RequestBody BookingRequest request) {
        log.info("Received booking request: {}", request);
        BookingResponse response = bookingService.bookTour(request);
        log.info("Booking successful: {}", response);
        return ApiResponse.<BookingResponse>builder().result(response).build();
    }
}
