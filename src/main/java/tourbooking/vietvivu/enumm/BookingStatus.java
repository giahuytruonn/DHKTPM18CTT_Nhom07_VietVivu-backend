package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CONFIRMED_CANCELLATION("Confirmed Cancellation"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String message;

    BookingStatus(String message) {
        this.message = message;
    }
}