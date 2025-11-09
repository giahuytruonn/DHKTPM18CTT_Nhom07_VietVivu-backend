package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.TourStatus;

import java.time.LocalDate;
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
          AND (:startDate IS NULL OR t.startDate = :startDate)
          AND (:durationDays IS NULL OR FUNCTION('DATEDIFF', DAY, t.startDate, t.endDate) = :durationDays)
          AND (:minQuantity IS NULL OR t.quantity >= :minQuantity)
          AND t.availability = true
        """)
    List<Tour> searchToursPublic(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("durationDays") Integer durationDays,
            @Param("minQuantity") Integer minQuantity);

    @Query("""
        SELECT t FROM Tour t
        WHERE (:keyword IS NULL 
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:destination IS NULL OR LOWER(t.destination) = LOWER(:destination))
          AND (:minPrice IS NULL OR t.priceAdult >= :minPrice)
          AND (:maxPrice IS NULL OR t.priceAdult <= :maxPrice)
          AND (:startDate IS NULL OR t.startDate = :startDate)
          AND (:durationDays IS NULL OR FUNCTION('DATEDIFF', DAY, t.startDate, t.endDate) = :durationDays)
          AND (:minQuantity IS NULL OR t.quantity >= :minQuantity)
          AND (:tourStatus IS NULL OR t.tourStatus = :tourStatus)
        """)
    List<Tour> searchToursAdmin(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("durationDays") Integer durationDays,
            @Param("minQuantity") Integer minQuantity,
            @Param("tourStatus") TourStatus tourStatus);
}