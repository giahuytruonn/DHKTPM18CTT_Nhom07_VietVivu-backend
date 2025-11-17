package tourbooking.vietvivu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.PaymentRequest;
import tourbooking.vietvivu.dto.request.PaymentSuccessRequest;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.PaymentSuccessResponse;
import tourbooking.vietvivu.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createPayment(@RequestBody PaymentRequest req) throws Exception {
        return ApiResponse.<Map<String, Object>>builder()
                .result(paymentService.createPayment(req))
                .message("Create link success")
                .build();
    }
    ;

    @PostMapping("/success")
    public ApiResponse<PaymentSuccessResponse> paymentSuccess(@RequestBody PaymentSuccessRequest request) {
        return ApiResponse.<PaymentSuccessResponse>builder()
                .result(paymentService.handlePaymentSuccess(request))
                .message("Payment success processed")
                .build();
    }
}
