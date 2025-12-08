package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUser_Id(String userId);

    List<Booking> findByUser(User user);

    Integer countByTourTourIdAndBookingStatus(String tourId, BookingStatus bookingStatus);

    Integer countByUserIdAndBookingStatus(String userId, BookingStatus bookingStatus);

    Integer countByBookingStatus(BookingStatus bookingStatus);
}
