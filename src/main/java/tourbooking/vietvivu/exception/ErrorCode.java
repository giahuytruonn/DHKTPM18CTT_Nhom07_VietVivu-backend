package tourbooking.vietvivu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1009, "Invalid credentials, please try again", HttpStatus.BAD_REQUEST),
    PASSWORD_EXISTED(1010, "Password existed", HttpStatus.BAD_REQUEST),
    INVALID_SERIALIZED_TOKEN(1011, "Invalid serialized unsecured/JWS/JWE", HttpStatus.BAD_REQUEST),
    TOUR_NOT_FOUND(1012, "Tour not found", HttpStatus.BAD_REQUEST),
    QUANTITY_NOT_ENOUGH(1013, "Quantity not enough", HttpStatus.BAD_REQUEST),
    DATE_NOT_AVAILABLE(1014, "Date not available", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_FOUND(1015, "Promotion not found", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(1016, "Promotion expired", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_APPLICABLE(1017, "Promotion not applicable", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_AVAILABLE(1018, "Promotion not available", HttpStatus.BAD_REQUEST),
    BOOKING_REQUEST_NOT_FOUND(1019, "Booking Request not found", HttpStatus.BAD_REQUEST),
    BOOKING_STATUS_INVALID(1020, "BookingRequest Invalid", HttpStatus.BAD_REQUEST),
    ACTION_TYPE_INVALID(1021, "Action Type Invalid", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1022, "Booking not found", HttpStatus.BAD_REQUEST),
    OLD_TOUR_NOT_FOUND(1023, "Old tour not found", HttpStatus.BAD_REQUEST),
    NEW_TOUR_NOT_FOUND(1024, "New tour not found", HttpStatus.BAD_REQUEST),
    USER_NOT_BELONG_BOOKING(1025, "User does not belong to the booking", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(1026, "Review already exists for this booking", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_COMPLETED(1027, "Booking is not completed yet", HttpStatus.BAD_REQUEST),
    TOUR_ALREADY_IN_FAVORITES(1028, "Tour is already in favorites", HttpStatus.BAD_REQUEST),
    TOUR_NOT_IN_FAVORITES(1029, "Tour is not in favorites", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(1030, "Review not found", HttpStatus.NOT_FOUND),
    CANNOT_CHANGE_TO_SAME_TOUR(1031, "Cannot change to the same tour", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(1032, "User not available", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1033, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1034, "OTP is expired", HttpStatus.BAD_REQUEST),
    TOKEN_RESET_INVALID(1035, "Reset token is invalid", HttpStatus.BAD_REQUEST),
    TOKEN_RESET_EXPIRED(1036, "Reset token is expired", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1037, "Email already existed", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1038, "Old password is incorrect", HttpStatus.BAD_REQUEST);

    // code 200 - OK

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
