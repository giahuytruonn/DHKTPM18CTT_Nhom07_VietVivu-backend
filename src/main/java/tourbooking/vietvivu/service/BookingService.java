package tourbooking.vietvivu.service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.BookingRequest;
import tourbooking.vietvivu.dto.response.BookingResponse;
import tourbooking.vietvivu.entity.*;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.enumm.PaymentStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.*;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final ContactRepository contactRepository;
    private final HistoryRepository historyRepository;

    public List<BookingResponse> getBookingByUserId(String userId) {
        List<Booking> bookings = bookingRepository.findByUser_Id(userId);
        return bookings.stream().map(this::mapToBookingResponse).collect(Collectors.toList());
    }

    public List<BookingResponse> getMyBookings() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        System.out.println(user.getUsername());
        System.out.println(user.getId());
        List<Booking> bookings = bookingRepository.findByUser_Id(user.getId());
        return bookings.stream().map(this::mapToBookingResponse).collect(Collectors.toList());
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        Tour tour = booking.getTour();
        Promotion promotion = booking.getPromotion();

        BookingResponse response = BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingDate(booking.getBookingDate())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .paymentTerm(booking.getPaymentTerm())
                .note(booking.getNote())
                .build();

        // Map user or contact information
        if (booking.getUser() != null) {
            User user = booking.getUser();
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhoneNumber());
            response.setAddress(user.getAddress());
        } else if (booking.getContact() != null) {
            Contact contact = booking.getContact();
            response.setName(contact.getName());
            response.setEmail(contact.getEmail());
            response.setPhone(contact.getPhoneNumber());
            response.setAddress(contact.getAddress());
        }

        // Map tour information
        if (tour != null) {
            response.setTourId(tour.getTourId());
            response.setTourTitle(tour.getTitle());
            response.setTourDuration(tour.getDuration());
            response.setTourDestination(tour.getDestination());
            response.setImageUrl(
                    tour.getImages() != null && !tour.getImages().isEmpty()
                            ? tour.getImages().stream()
                                    .findFirst()
                                    .map(Image::getImageUrl)
                                    .orElse(null)
                            : null);
            response.setPriceAdult(tour.getPriceAdult());
            response.setPriceChild(tour.getPriceChild());
        }

        // Map promotion information
        if (promotion != null) {
            response.setPromotionCode(promotion.getPromotionId());
            response.setDiscountAmount(promotion.getDiscount());
            if (booking.getTotalPrice() != null && promotion.getDiscount() != null) {
                response.setRemainingAmount(booking.getTotalPrice() - promotion.getDiscount());
            }
        }

        // Map booking details
        if (booking.getNumAdults() != null) {
            response.setNumOfAdults(booking.getNumAdults());
            if (tour != null && tour.getPriceAdult() != null) {
                response.setTotalPriceAdults(booking.getNumAdults() * tour.getPriceAdult());
            }
        }

        if (booking.getNumChildren() != null) {
            response.setNumOfChildren(booking.getNumChildren());
            if (tour != null && tour.getPriceChild() != null) {
                response.setTotalPriceChildren(booking.getNumChildren() * tour.getPriceChild());
            }
        }

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse bookTour(BookingRequest request) {
        Tour tour = tourRepository
                .findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Check truoc 10 ngay moi duoc dat tour
        if (request.getBookingDate().isAfter(tour.getStartDate().minusDays(10))) {
            throw new AppException(ErrorCode.DATE_NOT_AVAILABLE);
        }

        // Check so luong nguoi dat tour
        if ((request.getNumOfAdults() + request.getNumOfChildren() > tour.getQuantity())) {
            throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);
        }

        // Check promotion not found
        Promotion promotion = promotionRepository
                .findById(request.getPromotionId())
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        // Check promotion expired
        if (promotion.getEndDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))) {
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);
        }

        // Check promotion not available
        if (!promotion.getStatus()) {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }

        Booking booking = new Booking();
        BookingResponse response = new BookingResponse();
        Contact contact = new Contact();

        booking.setTour(tour);

        // Check user
        if (request.getUserId() == null) {
            response = BookingResponse.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .note(request.getNote())
                    .build();

            contact = Contact.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhone())
                    .address(request.getAddress())
                    .build();
            booking.setContact(contact);
            booking.setUser(null);
        } else {
            // Lay thong tin user de luu vao booking response
            var user = userRepository
                    .findByUsername(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            response = BookingResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .address(user.getAddress())
                    .note(request.getNote())
                    .build();

            booking.setUser(user);
            booking.setContact(null);
        }
        booking.setBookingDate(LocalDateTime.now());
        booking.setNumAdults(request.getNumOfAdults());
        booking.setNumChildren(request.getNumOfChildren());
        Double totalPrice =
                (request.getNumOfAdults() * tour.getPriceAdult()) + (request.getNumOfChildren() * tour.getPriceChild());
        booking.setTotalPrice(totalPrice);
        booking.setNote(request.getNote());
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentTerm(tour.getStartDate().minusDays(7).atStartOfDay());
        booking.setPromotion(promotion);
        promotion.setQuantity(promotion.getQuantity() - 1);
        if (promotion.getQuantity() == 0) {
            promotion.setStatus(false);
        }

        //        Booking savedBooking = bookingRepository.save(booking);
        if (request.getNumOfChildren() + request.getNumOfAdults() <= tour.getQuantity()) {
            tour.setQuantity(tour.getQuantity() - (request.getNumOfChildren() + request.getNumOfAdults()));
            if (tour.getQuantity() == 0) {
                tour.setAvailability(false);
            }
            tourRepository.save(tour);
        }
        promotionRepository.save(promotion);
        bookingRepository.save(booking);

        History history = History.builder()
                .tourId(tour.getTourId())
                .actionType(ActionType.BOOK_TOUR)
                .timestamp(LocalDateTime.now())
                .build();

        if (booking.getUser() != null) {
            User user = booking.getUser();
            history.setUser(user);
            history.setContact(null);

            if (user.getHistories() == null) {
                user.setHistories(new HashSet<>());
            }
            user.getHistories().add(history);

            if (user.getBookings() == null) {
                user.setBookings(new HashSet<>());
            }
            user.getBookings().add(booking);

            userRepository.save(user);
        } else {
            history.setUser(null);
            history.setContact(contact);

            contact.setBooking(booking);

            if (contact.getHistories() == null) {
                contact.setHistories(new HashSet<>());
            }
            contact.getHistories().add(history);

            contactRepository.save(contact);
        }

        historyRepository.save(history);

        response.setBookingId(booking.getBookingId());
        response.setBookingDate(booking.getBookingDate());
        response.setTotalPrice(booking.getTotalPrice());
        response.setPromotionCode(promotion.getPromotionId());
        response.setDiscountAmount(promotion.getDiscount());
        response.setRemainingAmount(booking.getTotalPrice() - promotion.getDiscount());
        response.setBookingStatus(booking.getBookingStatus());
        response.setPaymentTerm(booking.getPaymentTerm());
        response.setTourId(tour.getTourId());
        response.setTourTitle(tour.getTitle());
        response.setTourDuration(tour.getDuration());
        response.setTourDestination(tour.getDestination());
        response.setImageUrl(tour.getImages().stream()
                .findFirst()
                .map(image -> image.getImageUrl())
                .orElse(null));
        response.setNumOfAdults(booking.getNumAdults());
        response.setPriceAdult(tour.getPriceAdult());
        response.setTotalPriceAdults(booking.getNumAdults() * tour.getPriceAdult());
        response.setNumOfChildren(booking.getNumChildren());
        response.setPriceChild(tour.getPriceChild());
        response.setTotalPriceChildren(booking.getNumChildren() * tour.getPriceChild());
        return response;
    }
}
