package tourbooking.vietvivu.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.ContactRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.service.EmailService;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendContact(@RequestBody @Valid ContactRequest request) {
        // Gọi service gửi mail
        emailService.sendContactEmail(request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Yêu cầu của bạn đã được gửi thành công!")
                .result("Chúng tôi sẽ phản hồi qua email sớm nhất.")
                .build());
    }
}
