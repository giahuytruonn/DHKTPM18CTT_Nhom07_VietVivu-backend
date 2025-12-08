package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Promotion;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    @Query("SELECT p FROM Promotion p WHERE p.endDate >= :today")
    List<Promotion> findAllActivePromotions(@Param("today") LocalDate today);

    @Query("SELECT p FROM Promotion p WHERE p.endDate < :today")
    List<Promotion> findAndUpdateStatus(@Param("today") LocalDate today);

}
