package tourbooking.vietvivu.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteTourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    @Transactional
    public void addToFavorites(String tourId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        if (!user.getFavoriteTours().contains(tour)) {
            user.getFavoriteTours().add(tour);
            userRepository.save(user);
        }
    }

    @Transactional
    public void removeFromFavorites(String tourId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        user.getFavoriteTours().remove(tour);
        userRepository.save(user);
    }

    public List<TourResponse> getMyFavoriteTours() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return user.getFavoriteTours().stream()
                .map(tourMapper::toTourResponse)
                .toList();
    }
}