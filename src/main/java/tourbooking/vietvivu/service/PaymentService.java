package tourbooking.vietvivu.service;

import java.time.LocalDate;

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

import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@Service
@RequiredArgsConstructor 
public class PaymentService {
    private final PayOS payOS;

    private final CheckoutRepository checkoutRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;


    public CheckoutResponseData createPayment(PaymentRequest request) throws Exception {
        long orderCode = System.currentTimeMillis() / 1000;

        ItemData item = ItemData.builder()
                .name("Tour ID: " + request.getTourId())
                .quantity(1)
                .price(Math.toIntExact(request.getAmount()))
                .build();

        PaymentData data = PaymentData.builder()
                .orderCode(orderCode)
                .amount(Math.toIntExact(request.getAmount()))
                .description(request.getDescription())
                .returnUrl("http://localhost:5173/payment-success")
                .cancelUrl("http://localhost:5173/payment-cancel")
                .item(item)
                .build();

        return payOS.createPaymentLink(data);
    }

 
    @Transactional
    public PaymentSuccessResponse handlePaymentSuccess(PaymentSuccessRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
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

        if(booking.getUser() != null) {
            email = booking.getUser().getEmail();
        }else{
            email = booking.getContact().getEmail();
        }
 
        emailService.sendInvoiceEmail(
                EmailRequest.builder()
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
                        .build()
        );

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