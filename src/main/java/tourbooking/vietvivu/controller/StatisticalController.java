package tourbooking.vietvivu.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /* ============================================================
    	1. TOP TOUR ĐƯỢC ĐẶT NHIỀU NHẤT
    ============================================================ */
    @GetMapping("/top-booked-tours")
    ApiResponse<Map<String, Integer>> getTopNTourBookedByStatus(
            @RequestParam(defaultValue = "ALL") String bookingStatus,
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LocalDateTime start = parseToDate(startTime, true);
        LocalDateTime end = parseToDate(endTime, false);

        String bs = bookingStatus.trim();

        if (bs.equalsIgnoreCase("ALL")) {
            return ApiResponse.<Map<String, Integer>>builder()
                    .result(statisticalService.getTopNTourBookedAllStatus(topN, start, end))
                    .build();
        }

        BookingStatus status = parseStatus(bs);

        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNTourBookedByStatus(topN, status, start, end))
                .build();
    }

    /* ============================================================
    	2. TOP USERS THEO TRẠNG THÁI
    ============================================================ */
    @GetMapping("/top-users")
    ApiResponse<Map<String, Integer>> getTopNCustomersByBookingStatus(
            @RequestParam(defaultValue = "ALL") String bookingStatus,
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LocalDateTime start = parseToDate(startTime, true);
        LocalDateTime end = parseToDate(endTime, false);

        String bs = bookingStatus.trim();

        if (bs.equalsIgnoreCase("ALL")) {
            return ApiResponse.<Map<String, Integer>>builder()
                    .result(statisticalService.getTopNCustomersAllStatus(topN, start, end))
                    .build();
        }

        BookingStatus status = parseStatus(bs);

        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNCustomersByBookingStatus(topN, status, start, end))
                .build();
    }

    /* ============================================================
    	3. ĐẾM BOOKINGS THEO TRẠNG THÁI
    ============================================================ */
    @GetMapping("/total-bookings-by-status")
    ApiResponse<Map<String, Integer>> getTotalBookingsByStatus(
            @RequestParam(defaultValue = "ALL") String bookingStatus,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LocalDateTime start = parseToDate(startTime, true);
        LocalDateTime end = parseToDate(endTime, false);

        if (bookingStatus.equalsIgnoreCase("ALL")) {
            return ApiResponse.<Map<String, Integer>>builder()
                    .result(statisticalService.getBookingCountAllStatus(start, end))
                    .build();
        }

        BookingStatus status = parseStatus(bookingStatus);

        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getBookingCountByStatus(status, start, end))
                .build();
    }

    /* ============================================================
    	4. TỔNG DOANH THU
    ============================================================ */
    @GetMapping("/total-revenue")
    ApiResponse<Double> getTotalRevenue() {
        return ApiResponse.<Double>builder()
                .result(statisticalService.getTotalRevenue())
                .build();
    }

    /* ============================================================
    	5. DOANH THU THEO THÁNG
    ============================================================ */
    @GetMapping("/revenue-by-month")
    ApiResponse<Map<String, Double>> getMonthlyRevenue(@RequestParam int year) {
        return ApiResponse.<Map<String, Double>>builder()
                .result(statisticalService.getMonthlyRevenue(year))
                .build();
    }

    // Thống kê theo phương thức thanh toán
    @GetMapping("/revenue-by-payment-method")
    ApiResponse<Map<String, Long>> getRevenueByPaymentMethod() {
        return ApiResponse.<Map<String, Long>>builder()
                .result(statisticalService.getRevenueByPaymentMethod())
                .build();
    }

    // Tống kê doanh thu theo tour trong khoảng thời gian
    @GetMapping("/revenue-by-tour")
    ApiResponse<Map<String, Double>> getRevenueByTour(
            @RequestParam String startTime,
            @RequestParam String endTime) {

        LocalDate start = LocalDate.parse(startTime);
        LocalDate end = LocalDate.parse(endTime);

        return ApiResponse.<Map<String, Double>>builder()
                .result(statisticalService.getRevenueByTour(start, end))
                .build();
    }

    /* ============================================================
    	6. HÀM HỖ TRỢ
    ============================================================ */

    private BookingStatus parseStatus(String raw) {
        try {
            return BookingStatus.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingStatus không hợp lệ: " + raw);
        }
    }

    private LocalDateTime parseToDate(String date, boolean isStart) {
        if (date == null || date.isBlank()) {
            return isStart ? LocalDateTime.of(1, 1, 1, 0, 0, 0) : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }

        return isStart ? LocalDateTime.parse(date + "T00:00:00") : LocalDateTime.parse(date + "T23:59:59");
    }

}
