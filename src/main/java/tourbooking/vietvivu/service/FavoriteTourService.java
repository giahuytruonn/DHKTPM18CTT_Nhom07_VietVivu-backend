package tourbooking.vietvivu.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.FavoriteTourRequest;
import tourbooking.vietvivu.dto.response.FavoriteTourResponse;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FavoriteTourService {
    UserRepository userRepository;
    TourRepository tourRepository;

    @Transactional
    public FavoriteTourResponse addFavoriteTour(FavoriteTourRequest request) {
        log.info("Adding favorite tour for request: {}", request);

        if (request == null
                || request.getTourId() == null
                || request.getTourId().isBlank()) {
            log.error("Invalid request: tourId is null or blank");
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        log.info("Current user: {}", username);

        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Validate tour exists
        if (!tourRepository.existsById(request.getTourId())) {
            log.error("Tour not found: {}", request.getTourId());
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        if (user.getFavouriteTours() == null) {
            user.setFavouriteTours(new HashSet<>());
        }

        if (user.getFavouriteTours().contains(request.getTourId())) {
            throw new AppException(ErrorCode.TOUR_ALREADY_IN_FAVORITES);
        }

        user.getFavouriteTours().add(request.getTourId());
        userRepository.save(user);

        return FavoriteTourResponse.builder()
                .favoriteTourIds(user.getFavouriteTours())
                .message("Tour đã được thêm vào danh sách yêu thích")
                .build();
    }

    @Transactional
    public FavoriteTourResponse removeFavoriteTour(String tourId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getFavouriteTours() == null || !user.getFavouriteTours().contains(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_IN_FAVORITES);
        }

        user.getFavouriteTours().remove(tourId);
        userRepository.save(user);

        return FavoriteTourResponse.builder()
                .favoriteTourIds(user.getFavouriteTours())
                .message("Tour đã được xóa khỏi danh sách yêu thích")
                .build();
    }

    public FavoriteTourResponse getFavoriteTours() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<String> favoriteTours = user.getFavouriteTours();
        if (favoriteTours == null) {
            favoriteTours = new HashSet<>();
        }

        return FavoriteTourResponse.builder()
                .favoriteTourIds(favoriteTours)
                .message("Danh sách tour yêu thích")
                .build();
    }

    public boolean isFavoriteTour(String tourId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getFavouriteTours() == null) {
            return false;
        }

        return user.getFavouriteTours().contains(tourId);
    }
}
