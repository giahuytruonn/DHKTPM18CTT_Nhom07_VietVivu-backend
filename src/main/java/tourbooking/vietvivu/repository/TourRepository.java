package tourbooking.vietvivu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import feign.Param;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.TourStatus;


@Repository
public interface TourRepository extends JpaRepository<Tour, String> {

    List<Tour> findByTitleContainingIgnoreCase(String title);

    List<Tour> findByDestinationContainingIgnoreCase(String destination);

    List<Tour> findByAvailability(Boolean availability);

    Tour findByTourId(String tourId);

    @Query(
            "SELECT t.tourId as id, t.title as title, t.priceAdult as priceAdult, t.priceChild as priceChild, t.duration as duration FROM Tour t WHERE t.tourId = :tourId")
    Map<String, Object> findTourSummaryById(@Param("tourId") String tourId);

    /**
     * Search tours cho PUBLIC (User & Guest)
     * - Chỉ trả về tours có availability = true và tourStatus = OPEN_BOOKING
     * - KHÔNG có filter tourStatus
     */
    @Query(
            """
			SELECT t FROM Tour t
			WHERE (:keyword IS NULL OR :keyword = ''
				OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:destination IS NULL OR :destination = ''
				OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
			AND (:minPrice IS NULL OR t.priceAdult >= :minPrice)
			AND (:maxPrice IS NULL OR t.priceAdult <= :maxPrice)
			AND (:startDate IS NULL OR t.startDate = :startDate)
			AND (:minQuantity IS NULL OR t.quantity >= :minQuantity)
			AND t.availability = true
			AND t.tourStatus = tourbooking.vietvivu.enumm.TourStatus.OPEN_BOOKING
			""")
    List<Tour> searchToursPublic(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("destination") String destination,
            @org.springframework.data.repository.query.Param("minPrice") Double minPrice,
            @org.springframework.data.repository.query.Param("maxPrice") Double maxPrice,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("minQuantity") Integer minQuantity);

    /**
     * Search tours cho ADMIN
     * - Trả về tất cả tours
     * - CÓ filter theo tourStatus
     */
    @Query(
            """
			SELECT t FROM Tour t
			WHERE (:keyword IS NULL OR :keyword = ''
				OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:destination IS NULL OR :destination = ''
				OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
			AND (:minPrice IS NULL OR t.priceAdult >= :minPrice)
			AND (:maxPrice IS NULL OR t.priceAdult <= :maxPrice)
			AND (:startDate IS NULL OR t.startDate = :startDate)
			AND (:minQuantity IS NULL OR t.quantity >= :minQuantity)
			AND (:tourStatus IS NULL OR t.tourStatus = :tourStatus)
			""")
    List<Tour> searchToursAdmin(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("minQuantity") Integer minQuantity,
            @Param("tourStatus") TourStatus tourStatus);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Tour t SET t.tourStatus =
        CASE
            WHEN t.endDate < CURRENT_TIMESTAMP THEN 'COMPLETED'
            WHEN t.startDate <= CURRENT_TIMESTAMP THEN 'IN_PROGRESS'
            ELSE 'OPEN_BOOKING'
        END
    """)
    void updateAllTourStatuses();
}
