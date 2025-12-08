package tourbooking.vietvivu.service;

import java.time.Year;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.EmailRequest;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendInvoiceEmail(EmailRequest req) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(req.getRecipient());
            helper.setSubject(req.getSubject());

            // Thymeleaf context
            Context context = new Context();
            context.setVariable("bookingId", req.getBookingId());
            context.setVariable("bookingDate", req.getBookingDate());
            context.setVariable("tourTitle", req.getTourTitle());
            context.setVariable("tourDestination", req.getTourDestination());
            context.setVariable("tourDuration", req.getTourDuration());
            context.setVariable("numAdults", req.getNumAdults());
            context.setVariable("numChildren", req.getNumChildren());
            context.setVariable("priceAdult", req.getPriceAdult());
            context.setVariable("priceChild", req.getPriceChild());
            context.setVariable("totalPrice", req.getTotalPrice());
            context.setVariable("discountAmount", req.getDiscountAmount());
            context.setVariable("finalAmount", req.getFinalAmount());
            context.setVariable("note", req.getNote());
            context.setVariable("paymentMethod", req.getPaymentMethod());
            context.setVariable("paymentStatus", req.getPaymentStatus());
            context.setVariable("transactionId", req.getTransactionId());
            context.setVariable("invoiceId", req.getInvoiceId());
            context.setVariable("invoiceDate", req.getInvoiceDate());
            context.setVariable("year", Year.now().getValue());

            String html = templateEngine.process("invoice-email", context);
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("✅ Email sent to " + req.getRecipient());
        } catch (MessagingException e) {
            throw new RuntimeException("❌ Error sending email: " + e.getMessage(), e);
        }
    }

    public void sendOTP(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Your OTP Code");

            // Thymeleaf context
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("year", Year.now().getValue());

            String html = templateEngine.process("otp-email", context);
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("✅ OTP Email sent to " + email);
        } catch (MessagingException e) {
            throw new RuntimeException("❌ Error sending OTP email: " + e.getMessage(), e);
        }
    }
}
