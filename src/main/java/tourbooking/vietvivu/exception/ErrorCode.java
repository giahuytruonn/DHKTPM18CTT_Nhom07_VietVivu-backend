package tourbooking.vietvivu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Lỗi không xác định", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Tên người dùng phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Tuổi của bạn phải ít nhất {min}", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1009, "Thông tin đăng nhập không hợp lệ, vui lòng thử lại", HttpStatus.BAD_REQUEST),
    PASSWORD_EXISTED(1010, "Mật khẩu đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_SERIALIZED_TOKEN(1011, "Token không hợp lệ (unsecured/JWS/JWE)", HttpStatus.BAD_REQUEST),
    TOUR_NOT_FOUND(1012, "Không tìm thấy tour", HttpStatus.BAD_REQUEST),
    QUANTITY_NOT_ENOUGH(1013, "Số lượng không đủ", HttpStatus.BAD_REQUEST),
    DATE_NOT_AVAILABLE(1014, "Ngày không khả dụng", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_FOUND(1015, "Không tìm thấy khuyến mãi", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(1016, "Khuyến mãi đã hết hạn", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_APPLICABLE(1017, "Khuyến mãi không áp dụng", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_AVAILABLE(1018, "Khuyến mãi không khả dụng", HttpStatus.BAD_REQUEST),
    BOOKING_REQUEST_NOT_FOUND(1019, "Không tìm thấy yêu cầu đặt tour", HttpStatus.BAD_REQUEST),
    BOOKING_STATUS_INVALID(1020, "Trạng thái yêu cầu đặt tour không hợp lệ", HttpStatus.BAD_REQUEST),
    ACTION_TYPE_INVALID(1021, "Loại hành động không hợp lệ", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1022, "Không tìm thấy đặt tour", HttpStatus.BAD_REQUEST),
    OLD_TOUR_NOT_FOUND(1023, "Không tìm thấy tour cũ", HttpStatus.BAD_REQUEST),
    NEW_TOUR_NOT_FOUND(1024, "Không tìm thấy tour mới", HttpStatus.BAD_REQUEST),
    USER_NOT_BELONG_BOOKING(1025, "Người dùng không thuộc về đặt tour này", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(1026, "Đánh giá đã tồn tại cho đặt tour này", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_COMPLETED(1027, "Đặt tour chưa hoàn thành", HttpStatus.BAD_REQUEST),
    TOUR_ALREADY_IN_FAVORITES(1028, "Tour đã có trong danh sách yêu thích", HttpStatus.BAD_REQUEST),
    TOUR_NOT_IN_FAVORITES(1029, "Tour không có trong danh sách yêu thích", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(1030, "Không tìm thấy đánh giá", HttpStatus.NOT_FOUND),
    CANNOT_CHANGE_TO_SAME_TOUR(1031, "Không thể đổi sang cùng một tour", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(1032, "Người dùng không khả dụng", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1033, "OTP không hợp lệ", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1034, "OTP đã hết hạn", HttpStatus.BAD_REQUEST),
    TOKEN_RESET_INVALID(1035, "Token đặt lại không hợp lệ", HttpStatus.BAD_REQUEST),
    TOKEN_RESET_EXPIRED(1036, "Token đặt lại đã hết hạn", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1037, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1038, "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),
    CONTACT_NOT_FOUND(1039, "Không tìm thấy liên hệ", HttpStatus.NOT_FOUND);

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
