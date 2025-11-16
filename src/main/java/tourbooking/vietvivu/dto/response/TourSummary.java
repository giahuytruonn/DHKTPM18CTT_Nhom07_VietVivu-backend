package tourbooking.vietvivu.dto.response;

import java.util.List;

public record TourSummary(
        String tourId,
        String name,
        String priceAdult,
        String priceChild,
        String days,
        String[] imageUrls
) {}
