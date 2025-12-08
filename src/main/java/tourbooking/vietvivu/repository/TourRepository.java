package tourbooking.vietvivu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.dto.response.TourSelectionResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.TourStatus;

@Repository
public interface TourRepository extends JpaRepository<Tour, String> {

    Tour findByTourId(String tourId);

    @Query(
            "SELECT t.tourId as id, t.title as title, t.priceAdult as priceAdult, t.priceChild as priceChild, t.duration as duration FROM Tour t WHERE t.tourId = :tourId")
    Map<String, Object> findTourSummaryById(@Param("tourId") String tourId);

    /**
     * Find all public tours with pagination
     */
    @Query(
            """
		SELECT t FROM Tour t
		WHERE t.availability = true
		AND t.tourStatus = tourbooking.vietvivu.enumm.TourStatus.OPEN_BOOKING
		""")
    Page<Tour> findAllPublicTours(Pageable pageable);

    /**
     * Search tours for PUBLIC with pagination
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
    Page<Tour> searchToursPublic(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("minQuantity") Integer minQuantity,
            Pageable pageable);

    /**
     * Search tours for ADMIN with pagination
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
    Page<Tour> searchToursAdmin(
            @Param("keyword") String keyword,
            @Param("destination") String destination,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("minQuantity") Integer minQuantity,
            @Param("tourStatus") TourStatus tourStatus,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(
            """
		UPDATE Tour t SET t.tourStatus =
			CASE
				WHEN t.endDate < CURRENT_TIMESTAMP THEN 'COMPLETED'
				WHEN t.startDate <= CURRENT_TIMESTAMP THEN 'IN_PROGRESS'
				ELSE 'OPEN_BOOKING'
			END
		""")
    void updateAllTourStatuses();

    @Query("SELECT new tourbooking.vietvivu.dto.response.TourSelectionResponse(t.id, t.title) FROM Tour t")
    List<TourSelectionResponse> findAllTourNames();
}
