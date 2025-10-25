package tourbooking.vietvivu.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;


@Configuration
public class RabbitMQConfig {
    public static final String BOOKING_QUEUE = "bookingQueue";

    @Bean
    public Queue bookingQueue(){
        return new Queue(BOOKING_QUEUE, true);
    }
}
