package tourbooking.vietvivu.repository;

import java.util.List;

import feign.Param;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Tour;

import java.util.List;
import java.util.Map;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {
    List<Tour> findByTitleContainingIgnoreCase(String title);

    List<Tour> findByDestinationContainingIgnoreCase(String destination);

    List<Tour> findByAvailability(Boolean availability);

    Tour findByTourId(String tourId);
    @Query("SELECT t.tourId as id, t.title as title, t.priceAdult as priceAdult, t.priceChild as priceChild, t.duration as duration FROM Tour t WHERE t.tourId = :tourId")
    Map<String, Object> findTourSummaryById(@Param("tourId") String tourId);

}
