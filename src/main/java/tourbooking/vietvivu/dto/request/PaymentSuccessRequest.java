package tourbooking.vietvivu.dto.request;

import lombok.*;
import tourbooking.vietvivu.enumm.PaymentMethod;
import tourbooking.vietvivu.enumm.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessRequest {
    private String bookingId;
    private Double amount;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
}

