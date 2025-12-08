package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.enumm.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUser_Id(String userId);

    List<Booking> getBookingsByBookingStatus(BookingStatus status);

    Integer countByTourTourIdAndBookingStatus(String tourId, BookingStatus bookingStatus);

    Integer countByUserIdAndBookingStatus(String userId, BookingStatus bookingStatus);

    Integer countByBookingStatus(BookingStatus bookingStatus);

    @Query(
            """
	SELECT t.title, COUNT(b)
	FROM Booking b
	JOIN b.tour t
	GROUP BY t.title
	ORDER BY COUNT(b) DESC
	LIMIT :topN
	""")
    List<Object[]> findTopNToursAll(int topN);

    @Query(
            """
	SELECT t.title, COUNT(b)
	FROM Booking b
	JOIN b.tour t
	WHERE b.bookingStatus = :status
	GROUP BY t.title
	ORDER BY COUNT(b) DESC
	LIMIT :topN
	""")
    List<Object[]> findTopNToursByStatus(BookingStatus status, int topN);
}
