package tourbooking.vietvivu.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.BookingRequest;

@Service
@RequiredArgsConstructor
public class BookingProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendBookingRequest(BookingRequest request) {
        rabbitTemplate.convertAndSend("bookingQueue", request);
    }
}
