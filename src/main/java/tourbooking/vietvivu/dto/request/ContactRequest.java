package tourbooking.vietvivu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotBlank(message = "Vui lòng chọn loại câu hỏi")
    private String topic; // Ví dụ: "Thanh toán", "Tour", "Khác"

    @NotBlank(message = "Nội dung không được để trống")
    private String message;

    private String customerName; // Tùy chọn
}
