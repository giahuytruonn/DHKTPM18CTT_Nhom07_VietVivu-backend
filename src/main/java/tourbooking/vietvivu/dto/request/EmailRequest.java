package tourbooking.vietvivu.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tourbooking.vietvivu.enumm.PaymentMethod;
import tourbooking.vietvivu.enumm.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String recipient;
    private String subject;

    private String bookingId;
    private LocalDateTime bookingDate;
    private String tourTitle;
    private String tourDestination;
    private String tourDuration;
    private Integer numAdults;
    private Integer numChildren;
    private Double priceAdult;
    private Double priceChild;
    private Double totalPrice;
    private Double discountAmount;
    private Double finalAmount;
    private String note;

    private String invoiceId;
    private LocalDate invoiceDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
}