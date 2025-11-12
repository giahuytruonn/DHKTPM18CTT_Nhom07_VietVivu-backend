package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.enumm.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Integer countByTourTourIdAndBookingStatus(String tourId, BookingStatus bookingStatus);

    Integer countByUserIdAndBookingStatus(String userId, BookingStatus bookingStatus);

    Integer countByBookingStatus(BookingStatus bookingStatus);
}
