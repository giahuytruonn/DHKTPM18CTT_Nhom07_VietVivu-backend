package tourbooking.vietvivu.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.service.StatisticalService;

@RestController
@RequestMapping("/statistical")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticalController {
    StatisticalService statisticalService;

    /**
     * Thống kê top N tour được đặt nhiều nhất
     * @param bookingStatus optional: ALL | PENDING | CONFIRMED | COMPLETED | CANCELLED
     * @param topN optional, default 5
     */
    @GetMapping("/top-booked-tours")
    ApiResponse<Map<String, Integer>> getTopNTourBookedByStatus(
            @RequestParam(required = false, defaultValue = "ALL") String bookingStatus,
            @RequestParam(required = false, defaultValue = "5") int topN) {

        // defensive: trim + upper
        String bs = bookingStatus == null ? "ALL" : bookingStatus.trim();

        if (bs.isBlank() || bs.equalsIgnoreCase("ALL")) {
            return ApiResponse.<Map<String, Integer>>builder()
                    .result(statisticalService.getTopNTourBookedAllStatus(topN))
                    .build();
        }

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(bs.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid bookingStatus provided to /top-booked-tours: {}", bookingStatus);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "BookingStatus không hợp lệ: " + bookingStatus
                            + ". Giá trị hợp lệ: ALL, PENDING, CONFIRMED, COMPLETED, CANCELLED");
        }

        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNTourBookedByStatus(topN, status))
                .build();
    }

    /**
     * Thống kê top N users theo trạng thái booking
     * @param bookingStatus optional: ALL | PENDING | CONFIRMED | COMPLETED | CANCELLED
     * @param topN optional, default 5
     */
    @GetMapping("/top-users")
    ApiResponse<Map<String, Integer>> getTopNCustomersByBookingStatus(
            @RequestParam(required = false, defaultValue = "ALL") String bookingStatus,
            @RequestParam(required = false, defaultValue = "5") int topN) {

        String bs = bookingStatus == null ? "ALL" : bookingStatus.trim();

        if (bs.isBlank() || bs.equalsIgnoreCase("ALL")) {
            return ApiResponse.<Map<String, Integer>>builder()
                    .result(statisticalService.getTopNCustomersAllStatus(topN))
                    .build();
        }

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(bs.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid bookingStatus provided to /top-users: {}", bookingStatus);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "BookingStatus không hợp lệ: " + bookingStatus
                            + ". Giá trị hợp lệ: ALL, PENDING, CONFIRMED, COMPLETED, CANCELLED");
        }

        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNCustomersByBookingStatus(topN, status))
                .build();
    }

    // Thống kê tổng số đơn đặt theo trạng thái
    @GetMapping("/total-bookings-by-status")
    ApiResponse<Map<String, Integer>> getTotalBookingsByStatus() {
        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getBookingCountByStatus())
                .build();
    }

    // Thống kê tổng doanh thu
    @GetMapping("/total-revenue")
    ApiResponse<Double> getTotalRevenue() {
        return ApiResponse.<Double>builder()
                .result(statisticalService.getTotalRevenue())
                .build();
    }
}
