package tourbooking.vietvivu.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.InvoiceRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticalService {

    BookingRepository bookingRepository;
    TourRepository tourRepository;
    UserRepository userRepository;
    InvoiceRepository invoiceRepository;

    /** Convert key null → "UNKNOWN" để không lỗi JSON */
    private String safeKey(Object key) {
        if (key == null) return "UNKNOWN";
        return key.toString();
    }

    /** Top tour theo 1 trạng thái */
    public Map<String, Integer> getTopNTourBookedByStatus(int topN, BookingStatus status) {
        List<Object[]> rows = (status == null)
                ? bookingRepository.findTopNToursAll(topN)
                : bookingRepository.findTopNToursByStatus(status, topN);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String tourName = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(tourName, count);
        }
        return result;
    }

    /** Top tour theo tất cả trạng thái */
    public Map<String, Integer> getTopNTourBookedAllStatus(int topN) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (BookingStatus status : BookingStatus.values()) {
            List<Object[]> rows = bookingRepository.findTopNToursByStatus(status, topN);

            for (Object[] row : rows) {
                String tourName = safeKey(row[0]);
                int count = ((Number) row[1]).intValue();
                result.put(tourName + " (" + status.name() + ")", count);
            }
        }

        return result;
    }

    /** Top users theo 1 trạng thái */
    public Map<String, Integer> getTopNCustomersByBookingStatus(int topN, BookingStatus status) {
        List<Object[]> rows = (status == null)
                ? userRepository.findTopNUsersAll(topN)
                : userRepository.findTopNUsersByStatus(status, topN);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String userName = safeKey(row[0]);
            int count = ((Number) row[1]).intValue();
            result.put(userName, count);
        }
        return result;
    }

    /** Top users theo tất cả trạng thái */
    public Map<String, Integer> getTopNCustomersAllStatus(int topN) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (BookingStatus status : BookingStatus.values()) {
            List<Object[]> rows = userRepository.findTopNUsersByStatus(status, topN);

            for (Object[] row : rows) {
                String userName = safeKey(row[0]);
                int count = ((Number) row[1]).intValue();
                result.put(userName + " (" + status.name() + ")", count);
            }
        }

        return result;
    }

    /** Tổng số bookings theo trạng thái */
    public Map<String, Integer> getBookingCountByStatus() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            int count = bookingRepository.countByBookingStatus(status);
            result.put(status.name(), count);
        }
        return result;
    }

    /** Tổng doanh thu từ invoices */
    public double getTotalRevenue() {
        Double totalRevenue = invoiceRepository.getTotalRevenue();
        return totalRevenue != null ? totalRevenue : 0.0;
    }
}
