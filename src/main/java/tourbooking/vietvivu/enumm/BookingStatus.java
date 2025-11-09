package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    PENDING_CANCELLATION("Pending cancellation"),
    CONFIRMED_CANCELLATION("Confirmed cancellation"),
    PENDING_CHANGE("Pending change"),
    CONFIRMED_CHANGE("Confirmed change");

    private final String message;

    BookingStatus(String message) {
        this.message = message;
    }
}
