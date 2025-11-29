package tourbooking.vietvivu.enumm;

import lombok.Getter;

@Getter
public enum TourStatus {
    OPEN_BOOKING("Đang mở booking"),
    IN_PROGRESS("Đang thực hiện"),
    COMPLETED("Đã hoàn thành");

    private final String message;

    TourStatus(String message) {
        this.message = message;
    }
}
