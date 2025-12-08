package tourbooking.vietvivu.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.PromotionResponse;
import tourbooking.vietvivu.entity.Promotion;
import tourbooking.vietvivu.repository.PromotionRepository;
import tourbooking.vietvivu.service.PromotionService;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionController {

    PromotionService promotionService;

    private final PromotionRepository promotionRepository;

    // ===== PUBLIC =====
    @GetMapping
    public ApiResponse<List<Promotion>> getAllPromotionsPublic() {
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            System.out.println(promotions);
            return ApiResponse.<List<Promotion>>builder()
                    .result(promotions)
                    .message("Retrieved " + promotions.size() + " promotions")
                    .build();
        } catch (Exception e) {
            log.error("Error getting promotions", e);
            return ApiResponse.<List<Promotion>>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    // ===== ADMIN =====
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Promotion>> getAllPromotionsAdmin() {
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            return ApiResponse.<List<Promotion>>builder()
                    .result(promotions)
                    .message("Retrieved " + promotions.size() + " promotions")
                    .build();
        } catch (Exception e) {
            log.error("Error getting promotions for admin", e);
            return ApiResponse.<List<Promotion>>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Promotion> createPromotion(@RequestBody Promotion promotion) {
        try {
            promotionService.createPromotion(promotion);
            return ApiResponse.<Promotion>builder()
                    .result(promotion)
                    .message("Promotion created successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error creating promotion", e);
            return ApiResponse.<Promotion>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Promotion> updatePromotion(@PathVariable String promotionId, @RequestBody Promotion promotion) {
        try {
            promotion.setPromotionId(promotionId);
            promotionService.updatePromotion(promotion);
            return ApiResponse.<Promotion>builder()
                    .result(promotion)
                    .message("Promotion updated successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error updating promotion " + promotionId, e);
            return ApiResponse.<Promotion>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePromotion(@PathVariable String promotionId) {
        try {
            promotionService.deletePromotion(promotionId);
            return ApiResponse.<Void>builder()
                    .message("Promotion deleted successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error deleting promotion " + promotionId, e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{promotionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Promotion> updatePromotionStatus(
            @PathVariable String promotionId, @RequestParam boolean status) {
        try {
            promotionService.updatePromotionStatus(promotionId, status);
            return ApiResponse.<Promotion>builder()
                    .result(promotionService.getAllPromotions().stream()
                            .filter(p -> p.getPromotionId().equals(promotionId))
                            .findFirst()
                            .orElse(null))
                    .message("Promotion status updated successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error updating promotion status " + promotionId, e);
            return ApiResponse.<Promotion>builder()
                    .code(500)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{promotionId}")
    public ApiResponse<PromotionResponse> getPromotion(@PathVariable String promotionId) {
        return ApiResponse.<PromotionResponse>builder()
                .result(promotionService.getActivePromotion(promotionId))
                .build();
    }
}
