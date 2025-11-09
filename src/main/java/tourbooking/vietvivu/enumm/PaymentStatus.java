package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PAID("Paid"),
    UNPAID("Unpaid");

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
