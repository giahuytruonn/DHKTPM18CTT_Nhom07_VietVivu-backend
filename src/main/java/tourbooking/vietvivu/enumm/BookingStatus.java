package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    CONFIRMED_CANCELLATION("Confirmed cancellation"),
    CONFIRMED_CHANGE("Confirmed change"),
    PENDING_CANCELLATION("Pending cancellation"),
    PENDING_CHANGE("Pending change"),
    DENIED_CANCELLATION("Denied cancellation"),
    DENIED_CHANGE("Denied change");

    private final String message;

    BookingStatus(String message) {
        this.message = message;
    }
}
