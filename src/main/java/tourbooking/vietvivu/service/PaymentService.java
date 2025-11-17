package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.EmailRequest;
import tourbooking.vietvivu.dto.request.PaymentRequest;
import tourbooking.vietvivu.dto.request.PaymentSuccessRequest;
import tourbooking.vietvivu.dto.response.PaymentSuccessResponse;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Checkout;
import tourbooking.vietvivu.entity.Invoice;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.enumm.PaymentStatus;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.CheckoutRepository;
import tourbooking.vietvivu.repository.InvoiceRepository;
import vn.payos.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PayOS payOS;

    private final CheckoutRepository checkoutRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public Map<String, Object> createPayment(PaymentRequest request) throws Exception {

        long orderCode = System.currentTimeMillis() / 1000;

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(request.getAmount())
                .description("Thanh toán tour #" )
                .returnUrl("http://localhost:5173/payment-success")
                .cancelUrl("http://localhost:5173/payment-cancel")
                .item(PaymentLinkItem.builder()
                        .name("Thanh toán tour")
                        .price(request.getAmount())
                        .quantity(1)
                        .build())
                .build();

        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

        Map<String, Object> result = new HashMap<>();
        result.put("checkoutUrl", response.getCheckoutUrl());
        result.put("qrCode", response.getQrCode()); // QR IMAGE URL
        result.put("paymentLinkId", response.getPaymentLinkId()); // dùng nhúng iframe

        return result;
    }


    @Transactional
    public PaymentSuccessResponse handlePaymentSuccess(PaymentSuccessRequest request) {
        Booking booking = bookingRepository
                .findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Checkout checkout = Checkout.builder()
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDate.now())
                .amount(request.getAmount())
                .paymentStatus(request.getPaymentStatus())
                .transactionId(request.getTransactionId())
                .booking(booking)
                .build();
        checkoutRepository.save(checkout);

        Invoice invoice = Invoice.builder()
                .amount(checkout.getAmount())
                .dateIssued(LocalDate.now())
                .details("Payment booking: " + booking.getBookingId())
                .checkout(checkout)
                .booking(booking)
                .build();
        invoiceRepository.save(invoice);

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        String email = "";

        if (booking.getUser() != null) {
            email = booking.getUser().getEmail();
        } else {
            email = booking.getContact().getEmail();
        }
        // Send invoice email
        emailService.sendInvoiceEmail(EmailRequest.builder()
                .recipient(email)
                .subject("Your Invoice for Booking " + booking.getBookingId())
                .bookingId(booking.getBookingId())
                .bookingDate(booking.getBookingDate())
                .tourTitle(booking.getTour().getTitle())
                .tourDestination(booking.getTour().getDestination())
                .tourDuration(booking.getTour().getDuration())
                .numAdults(booking.getNumAdults())
                .numChildren(booking.getNumChildren())
                .priceAdult(booking.getTour().getPriceAdult())
                .priceChild(booking.getTour().getPriceChild())
                .totalPrice(booking.getTotalPrice())
                .discountAmount(booking.getTotalPrice() - checkout.getAmount())
                .finalAmount(checkout.getAmount())
                .note(booking.getNote())
                .paymentMethod(checkout.getPaymentMethod())
                .paymentStatus(checkout.getPaymentStatus())
                .transactionId(checkout.getTransactionId())
                .invoiceId(invoice.getInvoiceId())
                .invoiceDate(invoice.getDateIssued())
                .build());

        return PaymentSuccessResponse.builder()
                .checkoutId(checkout.getCheckoutId())
                .invoiceId(invoice.getInvoiceId())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .bookingId(booking.getBookingId())
                .paymentDate(checkout.getPaymentDate())
                .invoiceDate(invoice.getDateIssued())
                .build();
    }
}
