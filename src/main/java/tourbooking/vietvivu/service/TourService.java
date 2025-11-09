package tourbooking.vietvivu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {
    TourRepository tourRepository;
    UserRepository userRepository;

    public List<TourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll();
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    public List<TourResponse> getAvailableTours() {
        List<Tour> tours = tourRepository.findAll().stream()
                .filter(tour -> tour.getAvailability() != null && tour.getAvailability())
                .collect(Collectors.toList());
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TourResponse getTourById(String tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return mapToTourResponse(tour);
    }

    public List<TourResponse> searchTours(String keyword) {
        List<Tour> tours = tourRepository.findByTitleContainingIgnoreCase(keyword);
        if (tours.isEmpty()) {
            tours = tourRepository.findByDestinationContainingIgnoreCase(keyword);
        }
        return tours.stream().map(this::mapToTourResponse).collect(Collectors.toList());
    }

    private TourResponse mapToTourResponse(Tour tour) {
        // Calculate average rating
        Double averageRating = null;
        Integer reviewCount = 0;
        if (tour.getReviews() != null && !tour.getReviews().isEmpty()) {
            reviewCount = tour.getReviews().size();
            averageRating = tour.getReviews().stream()
                    .filter(review -> review.getRating() != null)
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
        }

        // Get image URLs
        List<String> imageUrls = new ArrayList<>();
        if (tour.getImages() != null && !tour.getImages().isEmpty()) {
            imageUrls = tour.getImages().stream()
                    .map(image -> image.getImageUrl())
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
        }

        // Check if tour is favorite for current user
        Boolean isFavorite = false;
        try {
            var context = SecurityContextHolder.getContext();
            if (context.getAuthentication() != null
                    && context.getAuthentication().isAuthenticated()) {
                String username = context.getAuthentication().getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && user.getFavouriteTours() != null) {
                    isFavorite = user.getFavouriteTours().contains(tour.getTourId());
                }
            }
        } catch (Exception e) {
            // User not authenticated, isFavorite remains false
            log.debug("User not authenticated, setting isFavorite to false");
        }

        return TourResponse.builder()
                .tourId(tour.getTourId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .quantity(tour.getQuantity())
                .priceAdult(tour.getPriceAdult())
                .priceChild(tour.getPriceChild())
                .duration(tour.getDuration())
                .destination(tour.getDestination())
                .availability(tour.getAvailability())
                .startDate(tour.getStartDate())
                .itinerary(tour.getItinerary())
                .imageUrls(imageUrls)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .isFavorite(isFavorite)
                .build();
    }
}
