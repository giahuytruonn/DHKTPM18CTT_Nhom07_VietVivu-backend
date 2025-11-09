package tourbooking.vietvivu.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourSearchRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.enumm.TourStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.repository.TourRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    ImageRepository imageRepository;
    TourMapper tourMapper;

    public List<TourResponse> searchTours(TourSearchRequest request) {
        boolean isAdmin = isAdmin();
        List<Tour> tours;

        if (isAdmin) {
            tours = tourRepository.searchToursAdmin(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getDurationDays(),
                    request.getMinQuantity(),
                    request.getTourStatus()
            );
        } else {
            tours = tourRepository.searchToursPublic(
                    request.getKeyword(),
                    request.getDestination(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getStartDate(),
                    request.getDurationDays(),
                    request.getMinQuantity()
            );
        }

        tours.forEach(this::updateTourStatus);
        return tourMapper.toTourResponseList(tours);
    }

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        Tour tour = tourMapper.toTour(request);

        if (tour.getInitialQuantity() == null) {
            tour.setInitialQuantity(request.getInitialQuantity());
        }

        tour.setQuantity(request.getInitialQuantity());
        tour.setAvailability(true);

        if (tour.getEndDate() == null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            tour.setEndDate(request.getStartDate().plusDays(durationDays));
        }

        if (tour.getEndDate().isBefore(tour.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        updateTourStatus(tour);
        tour = tourRepository.save(tour);
        saveImages(tour, request.getImageUrls());
        return tourMapper.toTourResponse(tour);
    }

    @Transactional
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        tourMapper.updateTour(tour, request);

        if (request.getImageUrls() != null) {
            imageRepository.deleteByTour_TourId(tourId);
            saveImages(tour, request.getImageUrls());
        }

        updateTourStatus(tour);
        tour = tourRepository.save(tour);
        return tourMapper.toTourResponse(tour);
    }

    @Transactional
    public void deleteTour(String tourId) {
        if (!tourRepository.existsById(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }
        tourRepository.deleteById(tourId);
    }

    public List<TourResponse> getAllTours() {
        List<Tour> tours = tourRepository.findAll();
        tours.forEach(this::updateTourStatus);
        return tourMapper.toTourResponseList(tours);
    }

    public TourResponse getTour(String tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        updateTourStatus(tour);
        return tourMapper.toTourResponse(tour);
    }

    private void saveImages(Tour tour, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        Set<Image> images = imageUrls.stream()
                .map(url -> Image.builder()
                        .imageUrl(url)
                        .uploadDate(LocalDate.now())
                        .tour(tour)
                        .build())
                .collect(Collectors.toSet());
        imageRepository.saveAll(images);
    }

    private void updateTourStatus(Tour tour) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = tour.getStartDate();
        LocalDate endDate = tour.getEndDate();

        if (endDate == null && tour.getDuration() != null) {
            int durationDays = extractDaysFromDuration(tour.getDuration());
            endDate = startDate.plusDays(durationDays);
            tour.setEndDate(endDate);
        }

        if (now.isBefore(startDate.minusDays(1))) {
            if (!tour.getAvailability()) {
                tour.setTourStatus(TourStatus.IN_PROGRESS);
            } else {
                tour.setTourStatus(TourStatus.OPEN_BOOKING);
            }
        } else if (!now.isAfter(endDate)) {
            tour.setTourStatus(TourStatus.IN_PROGRESS);
        } else {
            tour.setTourStatus(TourStatus.COMPLETED);
        }
    }

    private int extractDaysFromDuration(String duration) {
        try {
            String[] parts = duration.split(" ");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            log.warn("Cannot parse duration: {}", duration);
            return 1;
        }
    }

    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}