package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.repository.BookingRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleService {

    private static final List<BookingStatus> ELIGIBLE_STATUSES = List.of(
            BookingStatus.CONFIRMED,
            BookingStatus.CONFIRMED_CHANGE,
            BookingStatus.DENIED_CANCELLATION,
            BookingStatus.DENIED_CHANGE);

    private final BookingRepository bookingRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void markCompletedBookings() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findBookingsToComplete(ELIGIBLE_STATUSES, today);
        if (bookings.isEmpty()) {
            return;
        }

        bookings.forEach(booking -> booking.setBookingStatus(BookingStatus.COMPLETED));
        bookingRepository.saveAll(bookings);
        log.info("Marked {} bookings as COMPLETED (tour end date before {})", bookings.size(), today);
    }
}
