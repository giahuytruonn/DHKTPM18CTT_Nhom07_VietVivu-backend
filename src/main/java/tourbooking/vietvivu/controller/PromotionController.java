package tourbooking.vietvivu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.dto.response.ApiResponse;
import tourbooking.vietvivu.dto.response.PromotionResponse;
import tourbooking.vietvivu.service.PromotionService;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {

    PromotionService promotionService;

    @GetMapping("/{promotionId}")
    public ApiResponse<PromotionResponse> getPromotion(@PathVariable String promotionId) {
        return ApiResponse.<PromotionResponse>builder()
                .result(promotionService.getActivePromotion(promotionId))
                .build();
    }
}
