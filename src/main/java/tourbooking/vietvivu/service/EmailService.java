package tourbooking.vietvivu.service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.ContactRequest;
import tourbooking.vietvivu.dto.request.EmailRequest;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.BookingRequest;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private static final Locale VI_LOCALE = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter VI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    public void sendBookingStatusNotification(
            Booking booking,
            BookingRequest bookingRequest,
            BookingStatus newStatus,
            double penaltyRate,
            double refundAmount) {
        String recipient = resolveRecipient(booking);
        if (recipient == null || recipient.isBlank()) {
            log.warn(
                    "Skip sending booking status email because recipient is missing. Booking {}",
                    booking.getBookingId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            NotificationCopy copy = buildCopy(newStatus);
            helper.setTo(recipient);
            helper.setSubject(copy.subject());

            Context context = new Context();
            String greetingName = resolveDisplayName(booking);
            String requestTypeLabel = bookingRequest.getRequestType() == ActionType.CHANGE ? "đổi tour" : "hủy tour";
            String tourTitle = bookingRequest.getOldTour() != null
                    ? bookingRequest.getOldTour().getTitle()
                    : booking.getTour() != null ? booking.getTour().getTitle() : "Tour của bạn";
            String newTourTitle = bookingRequest.getNewTour() != null
                    ? bookingRequest.getNewTour().getTitle()
                    : null;
            String departureDate = resolveDepartureDate(bookingRequest);
            String penaltyLabel = Math.round(penaltyRate * 100) + "% tổng giá tour";
            double total = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0d;
            String penaltyAmount = formatCurrency(total * penaltyRate);
            String totalFormatted = formatCurrency(total);
            String refundFormatted = formatCurrency(refundAmount);

            context.setVariable("brandName", "VietVivu");
            context.setVariable("statusTitle", copy.title());
            context.setVariable("introMessage", copy.intro());
            context.setVariable("statusMessage", copy.message());
            context.setVariable("greeting", greetingName);
            context.setVariable("bookingId", booking.getBookingId());
            context.setVariable("tourTitle", tourTitle);
            context.setVariable("newTourTitle", newTourTitle);
            context.setVariable("hasNewTour", newTourTitle != null && !newTourTitle.isBlank());
            context.setVariable("departureDate", departureDate);
            context.setVariable("requestTypeLabel", requestTypeLabel);
            context.setVariable("requestReason", bookingRequest.getReason());
            context.setVariable(
                    "reasonAvailable",
                    bookingRequest.getReason() != null
                            && !bookingRequest.getReason().isBlank());
            context.setVariable("penaltyLabel", penaltyLabel);
            context.setVariable("policySummary", buildPolicySummary(penaltyRate));
            context.setVariable("refundAmount", refundFormatted);
            context.setVariable("penaltyAmount", penaltyAmount);
            context.setVariable("totalPrice", totalFormatted);
            context.setVariable(
                    "showRefund",
                    newStatus == BookingStatus.CONFIRMED_CANCELLATION || newStatus == BookingStatus.CONFIRMED_CHANGE);
            context.setVariable("supportNote", copy.supportNote());
            context.setVariable("statusColor", copy.statusColor());
            context.setVariable("accentColor", copy.accentColor());
            context.setVariable("footerNote", "Cảm ơn bạn đã luôn đồng hành cùng VietVivu.");
            context.setVariable("currentYear", Year.now().getValue());

            String html = templateEngine.process("booking-status-email", context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Booking status email sent to {}", recipient);
        } catch (MessagingException e) {
            throw new RuntimeException("❌ Error sending booking status email: " + e.getMessage(), e);
        }
    }

    private record NotificationCopy(
            String subject,
            String title,
            String intro,
            String message,
            String supportNote,
            String statusColor,
            String accentColor) {}

    private NotificationCopy buildCopy(BookingStatus status) {
        return switch (status) {
            case CONFIRMED_CANCELLATION -> new NotificationCopy(
                    "VietVivu - Yêu cầu hủy tour đã được xác nhận",
                    "Yêu cầu hủy tour được duyệt",
                    "Cảm ơn bạn đã kiên nhẫn chờ đội ngũ VietVivu xác nhận.",
                    "Chúng tôi đã tiếp nhận việc hủy tour và sẽ hoàn tiền sau khi khấu trừ phí theo chính sách.",
                    "Khoản hoàn dự kiến sẽ được xử lý trong vòng 3-5 ngày làm việc.",
                    "#047857",
                    "#10b981");
            case CONFIRMED_CHANGE -> new NotificationCopy(
                    "VietVivu - Yêu cầu đổi tour đã được xác nhận",
                    "Bạn đã đổi tour thành công",
                    "Chúng tôi đã cập nhật hành trình theo đúng mong muốn của bạn.",
                    "Tour mới đã được xác nhận. Phần tiền hoàn (nếu có) sẽ được gửi lại sau khi khấu trừ phí.",
                    "Đội ngũ VietVivu sẽ chủ động liên hệ nếu cần bổ sung thông tin thanh toán.",
                    "#1d4ed8",
                    "#3b82f6");
            case DENIED_CANCELLATION -> new NotificationCopy(
                    "VietVivu - Phản hồi yêu cầu hủy tour",
                    "Rất tiếc, chúng tôi chưa thể hủy tour",
                    "Chúng tôi hiểu mong muốn thay đổi kế hoạch của bạn.",
                    "Hiện tại tour đang trong giai đoạn không thể hủy theo chính sách vận hành.",
                    "Nếu bạn vẫn cần hỗ trợ, vui lòng phản hồi email này hoặc liên hệ hotline 1900 9999.",
                    "#b91c1c",
                    "#f97316");
            case DENIED_CHANGE -> new NotificationCopy(
                    "VietVivu - Phản hồi yêu cầu đổi tour",
                    "Yêu cầu đổi tour chưa thể thực hiện",
                    "Đội ngũ VietVivu đã xem xét kỹ yêu cầu của bạn.",
                    "Rất tiếc tour bạn mong muốn không còn khả dụng hoặc không đảm bảo điều kiện đổi.",
                    "Chúng tôi sẽ ưu tiên hỗ trợ nếu bạn chọn một lịch trình khác trong thời gian tới.",
                    "#b45309",
                    "#fb923c");
            default -> new NotificationCopy(
                    "VietVivu - Cập nhật trạng thái booking",
                    "Cập nhật từ VietVivu",
                    "Chúng tôi xin thông báo về yêu cầu gần đây của bạn.",
                    "Đội ngũ đang tiếp tục xử lý yêu cầu.",
                    "Liên hệ chúng tôi nếu cần hỗ trợ thêm.",
                    "#0f172a",
                    "#6366f1");
        };
    }

    private String resolveRecipient(Booking booking) {
        if (booking.getUser() != null && booking.getUser().getEmail() != null) {
            return booking.getUser().getEmail();
        }
        if (booking.getContact() != null) {
            return booking.getContact().getEmail();
        }
        return null;
    }

    private String resolveDisplayName(Booking booking) {
        String name = null;
        if (booking.getUser() != null) {
            name = booking.getUser().getName();
        } else if (booking.getContact() != null) {
            name = booking.getContact().getName();
        }
        if (name == null || name.isBlank()) {
            return "Quý khách";
        }
        return name;
    }

    private String resolveDepartureDate(BookingRequest bookingRequest) {
        LocalDate startDate = null;
        if (bookingRequest.getOldTour() != null) {
            startDate = bookingRequest.getOldTour().getStartDate();
        } else if (bookingRequest.getBooking().getTour() != null) {
            startDate = bookingRequest.getBooking().getTour().getStartDate();
        }
        return startDate != null ? startDate.format(VI_DATE_FORMAT) : "Chưa cập nhật";
    }

    private String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VI_LOCALE);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(Math.max(0, Math.round(value)));
    }

    private String buildPolicySummary(double rate) {
        if (rate >= 1d) {
            return "Áp dụng mức phạt 100% vì yêu cầu được gửi trong vòng 7 ngày trước khởi hành.";
        }
        if (rate >= 0.5d) {
            return "Áp dụng mức phạt 50% cho yêu cầu trong khoảng 15-8 ngày trước khởi hành.";
        }
        return "Áp dụng mức phạt 25% vì yêu cầu được gửi sớm hơn 15 ngày trước khởi hành.";
    }

    @Value("${spring.mail.username}")
    private String fromEmail; // Email hệ thống gửi đi

    @Value("${app.consulting-email}")
    private String consultingEmail; // Email ban tư vấn nhận

    @Async // Chạy ngầm để không bắt người dùng chờ lâu
    public void sendContactEmail(ContactRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(consultingEmail); // Gửi đến ban tư vấn
            message.setSubject("[VietVivu] Thắc mắc mới từ khách hàng: " + request.getTopic());

            // Nội dung email gửi cho nhân viên tư vấn
            String content = String.format(
                    """
				Hệ thống nhận được yêu cầu hỗ trợ mới:
				--------------------------------------
				- Khách hàng: %s
				- Email phản hồi: %s
				- Chủ đề: %s

				- Nội dung câu hỏi:
				%s
				--------------------------------------
				Vui lòng phản hồi khách hàng qua email trên.
				""",
                    request.getCustomerName() != null ? request.getCustomerName() : "Ẩn danh",
                    request.getCustomerEmail(),
                    request.getTopic(),
                    request.getMessage());

            message.setText(content);
            mailSender.send(message);
            log.info("Đã gửi mail contact thành công tới ban tư vấn");

        } catch (Exception e) {
            log.error("Lỗi gửi mail contact: ", e);
            // Có thể throw exception nếu muốn handle kỹ hơn
        }
    }
}
