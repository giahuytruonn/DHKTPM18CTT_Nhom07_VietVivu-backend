package tourbooking.vietvivu.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.producer.BookingProducer;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingController {
    BookingProducer bookingProducer;

    @PostMapping
    public ApiResponse<Void> bookTour(@RequestBody BookingRequest request) {
        bookingProducer.sendBookingRequest(request);
        return ApiResponse.<Void>builder().message("Booking request has been sent to the queue").build();
    }
}
