package tourbooking.vietvivu.dto.response;

public record TourSummary(
        String tourId, String name, String priceAdult, String priceChild, String days, String[] imageUrls) {}
