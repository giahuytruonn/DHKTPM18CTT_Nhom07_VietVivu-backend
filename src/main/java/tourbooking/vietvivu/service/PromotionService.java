package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.PromotionResponse;
import tourbooking.vietvivu.entity.Promotion;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.PromotionRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionService {
    private final PromotionRepository promotionRepository;

    // CRUD Promotion
    public void createPromotion(Promotion promotion) {
        promotion.setPromotionId(UUID.randomUUID().toString().substring(0, 8));
        promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        promotionRepository.findAndUpdateStatus(LocalDate.now()).forEach(p -> {
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
        Promotion promotion = promotionRepository
                .findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found: " + promotionId));
        promotion.setStatus(status);
        promotionRepository.save(promotion);
    }

    public PromotionResponse getActivePromotion(String promotionId) {
        Promotion promotion = promotionRepository
                .findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        LocalDate today = LocalDate.now();
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(today)) {
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);
        }
        if (!Boolean.TRUE.equals(promotion.getStatus())) {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }
        if (promotion.getQuantity() == null || promotion.getQuantity() <= 0) {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }

        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .description(promotion.getDescription())
                .discount(promotion.getDiscount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus())
                .quantity(promotion.getQuantity())
                .build();
    }
}
