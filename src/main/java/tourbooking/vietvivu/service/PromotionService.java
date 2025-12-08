package tourbooking.vietvivu.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.entity.Promotion;
import tourbooking.vietvivu.repository.PromotionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionService {

    PromotionRepository promotionRepository;
    //CRUD Promotion
    public void createPromotion(Promotion promotion) {
        promotion.setPromotionId(UUID.randomUUID().toString().substring(0, 8));
        promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        promotionRepository.findAndUpdateStatus(LocalDate.now()).forEach(p ->{
            p.setStatus(false);
            promotionRepository.save(p);
        });
        return promotionRepository.findAllActivePromotions(LocalDate.now());
    }

    public void deletePromotion(String id) {
        promotionRepository.deleteById(id);
    }

    public void updatePromotion(Promotion promotion) {
        promotionRepository.save(promotion);
    }

    public void updatePromotionStatus(String promotionId, boolean status) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found: " + promotionId));
        promotion.setStatus(status);
        promotionRepository.save(promotion);
    }


}
