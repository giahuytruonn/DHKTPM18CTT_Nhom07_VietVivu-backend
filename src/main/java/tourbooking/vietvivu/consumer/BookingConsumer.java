package tourbooking.vietvivu.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.BookingResponse;
import tourbooking.vietvivu.service.BookingService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingConsumer {
    private final BookingService bookingService;

    @RabbitListener(queues = "bookingQueue")
    public void processBooking(BookingRequest request) {
        try {
            // Xử lý logic đặt tour
            BookingResponse response = bookingService.bookTour(request.getUserId(), request);
            log.info("Booking processed successfully: {}", response);
        } catch (Exception e) {
            log.error("Failed to process booking: {}", e.getMessage());
        }
    }
}
