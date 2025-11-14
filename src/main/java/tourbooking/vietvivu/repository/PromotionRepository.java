package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tourbooking.vietvivu.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    // Check theo tour su dung duoc
}
