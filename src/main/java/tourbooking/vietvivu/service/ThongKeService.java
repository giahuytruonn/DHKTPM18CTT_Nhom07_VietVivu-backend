package tourbooking.vietvivu.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.TourRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ThongKeService {
    BookingRepository bookingRepository;
    TourRepository tourRepository;

    public Map<String, Integer> getTourBookingCounts(){
        List<Tour> tours = tourRepository.findAll();
        return tours.stream().collect(
                java.util.stream.Collectors.toMap(
                        Tour::getDestination,
                        tour -> bookingRepository.countByTourTourId(tour.getTourId())
                )
        );
    }

}
