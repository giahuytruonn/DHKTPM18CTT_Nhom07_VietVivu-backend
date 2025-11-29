package tourbooking.vietvivu.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // Thống kê top N tour được đặt nhiều nhất(booking_status: CONFIRMED/ CANCELLED) Map<Tên tour, Số lần đặt>
    @GetMapping("/top-booked-tours")
    ApiResponse<Map<String, Integer>> getTopNTourBookedByStatus(@RequestParam String bookingStatus) {
        BookingStatus status;
        try {
            status = BookingStatus.valueOf(bookingStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("BookingStatus không hợp lệ");
        }

        int topN = 5;
        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNTourBookedByStatus(topN, status))
                .build();
    }

    @GetMapping("/top-users")
    ApiResponse<Map<String, Integer>> getTopNCustomersByBookingStatus(@RequestParam String bookingStatus) {
        BookingStatus status;
        try {
            status = BookingStatus.valueOf(bookingStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("BookingStatus không hợp lệ");
        }

        int topN = 5;
        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getTopNCustomersByBookingStatus(topN, status))
                .build();
    }

    // Thống kê tổng số đơn đặt theo trạng thái(booking_status: CONFIRMED/ CANCELLED/ PENDING) Map<Trang Thai, Số lần>
    @GetMapping("/total-bookings-by-status")
    ApiResponse<Map<String, Integer>> getTotalBookingsByStatus() {
        return ApiResponse.<Map<String, Integer>>builder()
                .result(statisticalService.getBookingCountByStatus())
                .build();
    }
}
