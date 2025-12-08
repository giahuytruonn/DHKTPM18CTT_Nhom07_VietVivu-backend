package tourbooking.vietvivu.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.BookingStatus;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCaseOrPhoneNumberContaining(String username, String phoneNumber);

    @Query("""
    SELECT u.name, COUNT(b)
    FROM Booking b
    JOIN b.user u
    WHERE b.bookingDate BETWEEN :startTime AND :endTime
    GROUP BY u.name
    ORDER BY COUNT(b) DESC
""")
    List<Object[]> findTopNUsersAllTime(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT u.name, COUNT(b)
    FROM Booking b
    JOIN b.user u
    WHERE b.bookingStatus = :status
      AND b.bookingDate BETWEEN :startTime AND :endTime
    GROUP BY u.name
    ORDER BY COUNT(b) DESC
""")
    List<Object[]> findTopNUsersByStatusAndTime(
            @Param("status") BookingStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );



}
