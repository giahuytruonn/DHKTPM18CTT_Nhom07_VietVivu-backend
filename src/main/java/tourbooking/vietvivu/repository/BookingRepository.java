package tourbooking.vietvivu.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUser_Id(String userId);

    List<Booking> getBookingsByBookingStatus(BookingStatus status);

    List<Booking> findByUser(User user);

    Integer countByTourTourIdAndBookingStatus(String tourId, BookingStatus bookingStatus);

    Integer countByUserIdAndBookingStatus(String userId, BookingStatus bookingStatus);

    Integer countByBookingStatus(BookingStatus bookingStatus);

    @Query(
            """
	SELECT t.title, COUNT(DISTINCT b.bookingId)
	FROM Booking b
	JOIN b.tour t
	WHERE b.bookingDate BETWEEN :startTime AND :endTime
	GROUP BY t.tourId, t.title
	ORDER BY COUNT(DISTINCT b.bookingId) DESC
""")
    List<Object[]> findTopNToursAllTime(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

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
	SELECT t.title, COUNT(DISTINCT b.bookingId)
	FROM Booking b
	JOIN b.tour t
	WHERE b.bookingStatus = :status
	AND b.bookingDate BETWEEN :startTime AND :endTime
	GROUP BY t.tourId, t.title
	ORDER BY COUNT(DISTINCT b.bookingId) DESC
""")
    List<Object[]> findTopNToursByStatusAndTime(
            @Param("status") BookingStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query(
            """
	SELECT b.bookingStatus, COUNT(b)
	FROM Booking b
	WHERE b.bookingDate BETWEEN :start AND :end
	GROUP BY b.bookingStatus
""")
    List<Object[]> countAllStatus(LocalDateTime start, LocalDateTime end);

    @Query(
            """
	SELECT b.bookingStatus, COUNT(b)
	FROM Booking b
	WHERE b.bookingStatus = :status
	AND b.bookingDate BETWEEN :start AND :end
	GROUP BY b.bookingStatus
""")
    List<Object[]> countByStatus(BookingStatus status, LocalDateTime start, LocalDateTime end);

    List<Booking> findByTourTourIdAndBookingStatus(String tourId, BookingStatus bookingStatus);

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

    @Query(
            """
			SELECT b FROM Booking b
			JOIN b.tour t
			WHERE b.bookingStatus IN :statuses
			AND t.endDate IS NOT NULL
			AND t.endDate < :currentDate
			""")
    List<Booking> findBookingsToComplete(
            @Param("statuses") List<BookingStatus> statuses, @Param("currentDate") LocalDate currentDate);

    List<Booking> findByTour(Tour tour);
}
