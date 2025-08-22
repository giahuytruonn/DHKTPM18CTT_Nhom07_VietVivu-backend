package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {
}
