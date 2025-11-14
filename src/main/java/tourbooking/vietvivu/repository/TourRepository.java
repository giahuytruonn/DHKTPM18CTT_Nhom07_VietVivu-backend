package tourbooking.vietvivu.repository;

import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import feign.Param;
import tourbooking.vietvivu.entity.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {
    Tour findByTourId(String tourId);

    @Query(
            "SELECT t.tourId as id, t.title as title, t.priceAdult as priceAdult, t.priceChild as priceChild, t.duration as duration FROM Tour t WHERE t.tourId = :tourId")
    Map<String, Object> findTourSummaryById(@Param("tourId") String tourId);
}
