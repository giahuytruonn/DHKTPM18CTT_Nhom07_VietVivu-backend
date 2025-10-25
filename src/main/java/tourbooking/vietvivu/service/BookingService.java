package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.BookingResponse;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse bookTour(String userId, BookingRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

//        if (tour.getAvailableSeats() < request.getNumberOfGuests()) {
//            throw new AppException(ErrorCode.NOT_ENOUGH_SEATS);
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        // Cập nhật số lượng chỗ còn lại
//        tour.setAvailableSeats(tour.getAvailableSeats() - request.getNumberOfGuests());
//        tourRepository.save(tour);

        // Tạo bản ghi booking
//        Booking booking = new Booking(null, tour, user, request.getNumberOfGuests(), LocalDateTime.now());
//        booking = bookingRepository.save(booking);
//
//        return new BookingResponse(
//                booking.getId(),
//                tour.getName(),
//                user.getUsername(),
//                booking.getNumberOfGuests(),
//                booking.getBookingDate()
//        );
        return null;
    }
}
