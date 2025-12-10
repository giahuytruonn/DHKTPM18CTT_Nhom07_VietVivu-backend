package tourbooking.vietvivu.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.CancelContactBookingRequest;
import tourbooking.vietvivu.dto.request.ContactRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.ContactResponse;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.service.ContactService;
import tourbooking.vietvivu.service.EmailService;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;
    private final ContactService contactService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendContact(@RequestBody @Valid ContactRequest request) {
        // Gọi service gửi mail
        emailService.sendContactEmail(request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Yêu cầu của bạn đã được gửi thành công!")
                .result("Chúng tôi sẽ phản hồi qua email sớm nhất.")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<PaginationResponse<ContactResponse>>> getAllContacts(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<ContactResponse> response = contactService.getAllContacts(page, size);
        return ResponseEntity.ok(ApiResponse.<PaginationResponse<ContactResponse>>builder()
                .message("Lấy danh sách contact thành công")
                .result(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{contactId}")
    public ResponseEntity<ApiResponse<ContactResponse>> getContactById(@PathVariable String contactId) {
        ContactResponse response = contactService.getContactById(contactId);
        return ResponseEntity.ok(ApiResponse.<ContactResponse>builder()
                .message("Lấy thông tin contact thành công")
                .result(response)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{contactId}/cancel")
    public ResponseEntity<ApiResponse<ContactResponse>> cancelBookingByContact(
            @PathVariable String contactId, @RequestBody(required = false) CancelContactBookingRequest request) {
        String reason = request != null ? request.getReason() : "Hủy bởi Admin";
        ContactResponse response = contactService.cancelBookingByContact(contactId, reason);
        return ResponseEntity.ok(ApiResponse.<ContactResponse>builder()
                .message("Hủy booking thành công")
                .result(response)
                .build());
    }
}
