package tourbooking.vietvivu.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.repository.BookingRepository;
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

    // Thống kê top N tour có(booking_status: CONFIRMED) nhều nhất Map<Tên tour, Số lần đặt>
    public Map<String, Integer> getTopNTourBookedByStatus(int topN, String bookingStatus) {
        List<Tour> tours = tourRepository.findAll();

        // Tạo một danh sách các cặp (Tour, Số lần đặt)
        List<Map.Entry<Tour, Integer>> tourBookingCounts = tours.stream()
                .map(tour -> Map.entry(
                        tour, bookingRepository.countByTourTourIdAndBookingStatus(tour.getTourId(), bookingStatus)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sắp xếp giảm dần theo số lần đặt
                .limit(topN) // Lấy top N
                .collect(Collectors.toList());

        // Chuyển đổi danh sách thành Map<Tên tour, Số lần đặt>
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Tour, Integer> entry : tourBookingCounts) {
            result.put(entry.getKey().getTitle(), entry.getValue());
        }

        return result;
    }

    // Thống kê top N khách hàng có(booking_status: CONFIRMED/CANCELLED) nhiều nhất Map<Tên Khách hàng, Số lần>
    public Map<String, Integer> getTopNCustomersByBookingStatus(int topN, String bookingStatus) {
        List<tourbooking.vietvivu.entity.User> users = userRepository.findAll();

        // Tạo một danh sách các cặp (User, Số lần đặt)
        List<Map.Entry<tourbooking.vietvivu.entity.User, Integer>> userBookingCounts = users.stream()
                .map(user ->
                        Map.entry(user, bookingRepository.countByUserIdAndBookingStatus(user.getId(), bookingStatus)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sắp xếp giảm dần theo số lần đặt
                .limit(topN) // Lấy top N
                .collect(Collectors.toList());

        // Chuyển đổi danh sách thành Map<Tên Khách hàng, Số lần>
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<tourbooking.vietvivu.entity.User, Integer> entry : userBookingCounts) {
            result.put(entry.getKey().getName(), entry.getValue());
        }

        return result;
    }

    // Thong ke so luong booking theo trang thai Map<Trang thai, So luong booking>

    public Map<String, Integer> getBookingCountByStatus() {
        List<String> statuses = List.of("CONFIRMED", "CANCELLED", "PENDING", "COMPLETED");

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String status : statuses) {
            int count = bookingRepository.countByBookingStatus(status);
            result.put(status, count);
        }

        return result;
    }
}
