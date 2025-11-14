package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUser_Id(String userId);
}
