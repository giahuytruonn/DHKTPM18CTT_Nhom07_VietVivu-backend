package tourbooking.vietvivu.repository;

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

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);


    List<User> findByUsernameContainingIgnoreCaseOrPhoneNumberContaining(String username, String phoneNumber);

    @Query("""
       SELECT u.name, COUNT(b)
       FROM Booking b
       JOIN b.user u
       GROUP BY u.name
       ORDER BY COUNT(b) DESC
       LIMIT :topN
       """)
    List<Object[]> findTopNUsersAll(int topN);

    @Query("""
       SELECT u.name, COUNT(b)
       FROM Booking b
       JOIN b.user u
       WHERE b.bookingStatus = :status
       GROUP BY u.name
       ORDER BY COUNT(b) DESC
       LIMIT :topN
       """)
    List<Object[]> findTopNUsersByStatus(BookingStatus status, int topN);


    User findUserByEmail(String email);
}
