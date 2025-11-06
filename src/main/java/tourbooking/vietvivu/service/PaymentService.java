package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.PaymentRequest;
import tourbooking.vietvivu.dto.response.PaymentResponse;
import vn.payos.PayOS;
import vn.payos.*;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PayOS payOS;

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
}

