package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Tour;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {

    @Query("""
        SELECT t FROM Tour t
        WHERE (:keyword IS NULL 
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:destination IS NULL OR LOWER(t.destination) = LOWER(:destination))
          AND (:minPrice IS NULL OR t.priceAdult >= :minPrice)
          AND (:maxPrice IS NULL OR t.priceAdult <= :maxPrice)
          AND t.availability = true
        """)
    List<Tour> searchTours(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}
