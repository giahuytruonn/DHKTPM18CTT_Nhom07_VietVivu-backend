package tourbooking.vietvivu.service;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.repository.TourRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TourService {
    TourRepository tourRepository;

    public List<Tour> findAllTours() {
        return tourRepository.findAll();
    }

    public Tour findTourById(String tourId) {
        return tourRepository.findByTourId(tourId);
    }

    public Map<String, Object> findTourSummaryById(String tourId) {
        return tourRepository.findTourSummaryById(tourId);
    }
}
