package tourbooking.vietvivu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {
    List<Tour> findByTitleContainingIgnoreCase(String title);

    List<Tour> findByDestinationContainingIgnoreCase(String destination);

    List<Tour> findByAvailability(Boolean availability);
}
