package tourbooking.vietvivu.dto.response;

import java.time.LocalDate;

import lombok.*;

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
