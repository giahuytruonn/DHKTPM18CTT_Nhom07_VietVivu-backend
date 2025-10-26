package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.BookingResponse;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Promotion;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.PromotionRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;

    @Transactional
    public BookingResponse bookTour(BookingRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        //Check truoc 10 ngay moi duoc dat tour
        if(request.getBookingDate().isAfter(tour.getStartDate().minusDays(10))){
            throw new AppException(ErrorCode.DATE_NOT_AVAILABLE);
        }

        //Check so luong nguoi dat tour
        if((request.getNumOfAdults() + request.getNumOfChildren() > tour.getQuantity())){
            throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);
        }


        //Check promotion not found
        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        //Check promotion expired
        if(promotion.getEndDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))){
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);
        }

//        //Check promotion applicable
//        if(!promotion.getApplicableTours().contains(tour)){
//            throw new AppException(ErrorCode.PROMOTION_NOT_APPLICABLE);
//        }
        //Check promotion not available
        if(!promotion.getStatus())
        {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }

        Booking booking = new Booking();
        BookingResponse response = new BookingResponse();

        booking.setTour(tour);

        //Check user
        if(request.getUserId().isBlank()){
            response = BookingResponse.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .note(request.getNote())
                    .build();

            booking.setUser(null);
        } else {
            //Lay thong tin user de luu vao booking response
            var user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            response = BookingResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .address(user.getAddress())
                    .note(request.getNote())
                    .build();

            booking.setUser(user);
        }



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
