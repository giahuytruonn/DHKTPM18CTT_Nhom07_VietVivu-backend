package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    VNPAY("VNPAY"),
    VIETQR("VietQR");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }
}
