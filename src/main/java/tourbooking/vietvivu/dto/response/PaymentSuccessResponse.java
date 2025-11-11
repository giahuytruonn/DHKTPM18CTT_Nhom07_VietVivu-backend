package tourbooking.vietvivu.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PaymentSuccessResponse {
    private String checkoutId;
    private String invoiceId;
    private Double amount;
    private String transactionId;
    private LocalDate paymentDate;
    private LocalDate invoiceDate;
    private String bookingId;
}
