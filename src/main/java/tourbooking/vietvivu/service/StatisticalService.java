package tourbooking.vietvivu.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.BookingStatus;
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

    public Map<String, Integer> getTopNTourBookedByStatus(int topN, BookingStatus bookingStatus) {
        List<Tour> tours = tourRepository.findAll();

        List<Map.Entry<Tour, Integer>> tourBookingCounts = tours.stream()
                .map(tour -> Map.entry(
                        tour, bookingRepository.countByTourTourIdAndBookingStatus(tour.getTourId(), bookingStatus)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Tour, Integer> entry : tourBookingCounts) {
            result.put(entry.getKey().getTitle(), entry.getValue());
        }

        return result;
    }

    public Map<String, Integer> getTopNCustomersByBookingStatus(int topN, BookingStatus bookingStatus) {
        List<tourbooking.vietvivu.entity.User> users = userRepository.findAll();

        List<Map.Entry<tourbooking.vietvivu.entity.User, Integer>> userBookingCounts = users.stream()
                .map(user ->
                        Map.entry(user, bookingRepository.countByUserIdAndBookingStatus(user.getId(), bookingStatus)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<tourbooking.vietvivu.entity.User, Integer> entry : userBookingCounts) {
            result.put(entry.getKey().getName(), entry.getValue());
        }

        return result;
    }

    public Map<String, Integer> getBookingCountByStatus() {
        BookingStatus[] statuses = BookingStatus.values();
        Map<String, Integer> result = new LinkedHashMap<>();
        for (BookingStatus status : statuses) {
            int count = bookingRepository.countByBookingStatus(status);
            result.put(status.name(), count);
        }
        return result;
    }
}
