package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Integer countByTourTourIdAndBookingStatus(String tourId, String bookingStatus);

    Integer countByUserIdAndBookingStatus(String userId, String bookingStatus);

    Integer countByBookingStatus(String bookingStatus);
}
