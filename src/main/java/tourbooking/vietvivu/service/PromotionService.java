package tourbooking.vietvivu.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.response.PromotionResponse;
import tourbooking.vietvivu.entity.Promotion;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.PromotionRepository;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

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
