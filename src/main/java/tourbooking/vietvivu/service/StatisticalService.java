package tourbooking.vietvivu.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.repository.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticalService {

    BookingRepository bookingRepository;
    TourRepository tourRepository;
    UserRepository userRepository;
    InvoiceRepository invoiceRepository;
    CheckoutRepository checkoutRepository;

    private String safeKey(Object key) {
        return key == null ? "UNKNOWN" : key.toString();
    }

    /* ============================================================
    	⬇️ 1. TOP TOUR THEO TRẠNG THÁI + TIME
    ============================================================ */
    public Map<String, Integer> getTopNTourBookedByStatus(
            int topN, BookingStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> rows = bookingRepository.findTopNToursByStatusAndTime(status, startTime, endTime).stream()
                .limit(topN)
                .toList();

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String tourName = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(tourName, count);
        }
        return result;
    }

    /* ============================================================
    	⬇️ 2. TOP TOUR TẤT CẢ TRẠNG THÁI + TIME
    ============================================================ */
    public Map<String, Integer> getTopNTourBookedAllStatus(int topN, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> rows = bookingRepository.findTopNToursAllTime(startTime, endTime).stream()
                .limit(topN)
                .toList();

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String tourName = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(tourName, count);
        }

        return result;
    }

    /* ============================================================
    	⬇️ 3. TOP USER THEO TRẠNG THÁI + TIME
    ============================================================ */
    public Map<String, Integer> getTopNCustomersByBookingStatus(
            int topN, BookingStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> rows = userRepository.findTopNUsersByStatusAndTime(status, startTime, endTime).stream()
                .limit(topN)
                .toList();

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String userName = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(userName, count);
        }
        return result;
    }

    /* ============================================================
    	⬇️ 4. TOP USER – TẤT CẢ TRẠNG THÁI + TIME
    ============================================================ */
    public Map<String, Integer> getTopNCustomersAllStatus(int topN, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (BookingStatus status : BookingStatus.values()) {
            List<Object[]> rows = userRepository.findTopNUsersAllTime(startTime, endTime).stream()
                    .limit(topN)
                    .toList();

            for (Object[] row : rows) {
                String userName = safeKey(row[0]);
                int count = ((Number) row[1]).intValue();
                result.put(userName + " (" + status.name() + ")", count);
            }
        }

        return result;
    }

    /* ============================================================
    	⬇️ 5. ĐẾM BOOKING THEO TRẠNG THÁI + TIME
    ============================================================ */
    public Map<String, Integer> getBookingCountAllStatus(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = bookingRepository.countAllStatus(start, end);
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String status = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(status, count);
        }

        return result;
    }

    public Map<String, Integer> getBookingCountByStatus(BookingStatus status, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = bookingRepository.countByStatus(status, start, end);
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String stat = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(stat, count);
        }

        return result;
    }

    /* ============================================================
    	⬇️ 6. DOANH THU - bạn yêu cầu không đổi
    ============================================================ */

    public double getTotalRevenue() {
        Double totalRevenue = invoiceRepository.getTotalRevenue();
        return totalRevenue != null ? totalRevenue : 0.0;
    }

    public Map<String, Double> getMonthlyRevenue(int year) {
        List<Object[]> rows = invoiceRepository.getAmountGroupedByMonth(year);
        Map<String, Double> result = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            result.put(String.format("%02d", month), 0.0);
        }

        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            double amount = ((Number) row[1]).doubleValue();
            result.put(String.format("%02d", month), amount);
        }

        return result;
    }

    // Thống kê theo phương thưc thanh toán
    public Map<String, Long> getRevenueByPaymentMethod() {
        List<Object[]> rows = checkoutRepository.countByPaymentMethod();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String method = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = ((Number) row[1]).longValue();
            result.put(method, count);
        }

        return result;
    }
}
