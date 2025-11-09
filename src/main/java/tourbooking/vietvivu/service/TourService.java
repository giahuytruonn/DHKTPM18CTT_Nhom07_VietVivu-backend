package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.TourCreateRequest;
import tourbooking.vietvivu.dto.request.TourUpdateRequest;
import tourbooking.vietvivu.dto.response.TourResponse;
import tourbooking.vietvivu.entity.Image;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.mapper.TourMapper;
import tourbooking.vietvivu.repository.ImageRepository;
import tourbooking.vietvivu.repository.TourRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {

    TourRepository tourRepository;
    ImageRepository imageRepository;
    TourMapper tourMapper;

    public List<TourResponse> searchTours(String keyword, String destination, Double minPrice, Double maxPrice) {
        var tours = tourRepository.searchTours(keyword, destination, minPrice, maxPrice);
        return tourMapper.toTourResponseList(tours);
    }

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        Tour tour = tourMapper.toTour(request);
        tour.setAvailability(true);
        tour = tourRepository.save(tour);
        saveImages(tour, request.getImageUrls());
        return tourMapper.toTourResponse(tour);
    }

    @Transactional
    public TourResponse updateTour(String tourId, TourUpdateRequest request) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        tourMapper.updateTour(tour, request);

        if (request.getImageUrls() != null) {
            imageRepository.deleteByTour_TourId(tourId);
            saveImages(tour, request.getImageUrls());
        }

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
        return tourMapper.toTourResponseList(tourRepository.findAll());
    }

    public TourResponse getTour(String tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
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
}
