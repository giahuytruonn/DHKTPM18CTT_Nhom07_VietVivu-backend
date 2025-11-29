package tourbooking.vietvivu.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteTourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    /**
     * Add tour to favorites - AUTHENTICATED USER
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void addToFavorites(String tourId) {
        try {
            String username =
                    SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Adding tour {} to favorites for user {}", tourId, username);

            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("User not found: {}", username);
                return new AppException(ErrorCode.USER_NOT_EXISTED);
            });

            Tour tour = tourRepository.findById(tourId).orElseThrow(() -> {
                log.error("Tour not found: {}", tourId);
                return new AppException(ErrorCode.TOUR_NOT_FOUND);
            });

            if (user.getFavoriteTours().contains(tour)) {
                log.warn("Tour {} already in favorites for user {}", tourId, username);
                return;
            }

            user.getFavoriteTours().add(tour);
            userRepository.save(user);
            log.info("Successfully added tour {} to favorites for user {}", tourId, username);

        } catch (AppException e) {
            log.error("AppException in addToFavorites: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in addToFavorites", e);
            throw new RuntimeException("Failed to add tour to favorites: " + e.getMessage(), e);
        }
    }

    /**
     * Remove tour from favorites - AUTHENTICATED USER
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void removeFromFavorites(String tourId) {
        try {
            String username =
                    SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Removing tour {} from favorites for user {}", tourId, username);

            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("User not found: {}", username);
                return new AppException(ErrorCode.USER_NOT_EXISTED);
            });

            Tour tour = tourRepository.findById(tourId).orElseThrow(() -> {
                log.error("Tour not found: {}", tourId);
                return new AppException(ErrorCode.TOUR_NOT_FOUND);
            });

            user.getFavoriteTours().remove(tour);
            userRepository.save(user);
            log.info("Successfully removed tour {} from favorites for user {}", tourId, username);

        } catch (AppException e) {
            log.error("AppException in removeFromFavorites: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in removeFromFavorites", e);
            throw new RuntimeException("Failed to remove tour from favorites: " + e.getMessage(), e);
        }
    }

    /**
     * Get my favorite tours - AUTHENTICATED USER
     */
    @PreAuthorize("isAuthenticated()")
    public List<TourResponse> getMyFavoriteTours() {
        try {
            String username =
                    SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Getting favorite tours for user {}", username);

            User user = userRepository.findByUsername(username).orElseThrow(() -> {
                log.error("User not found: {}", username);
                return new AppException(ErrorCode.USER_NOT_EXISTED);
            });

            List<TourResponse> favoriteTours = user.getFavoriteTours().stream()
                    .map(tourMapper::toTourResponse)
                    .collect(Collectors.toList());

            log.info("Found {} favorite tours for user {}", favoriteTours.size(), username);
            return favoriteTours;

        } catch (AppException e) {
            log.error("AppException in getMyFavoriteTours: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getMyFavoriteTours", e);
            throw new RuntimeException("Failed to get favorite tours: " + e.getMessage(), e);
        }
    }
}
