package tourbooking.vietvivu.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.BookingCancelUpdateRequest;
import tourbooking.vietvivu.dto.request.BookingRequestStatusUpdateRequest;
import tourbooking.vietvivu.dto.response.BookingRequestResponse;
import tourbooking.vietvivu.entity.*;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.*;

@Service
@RequiredArgsConstructor
public class BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HistoryRepository historyRepository;
    private final TourRepository tourRepository;

    @Transactional(rollbackFor = Exception.class)
    public BookingRequestResponse updateBookingRequestStatusAdmin(
            String requestId, String adminId, BookingRequestStatusUpdateRequest request) {

        BookingRequest bookingRequest = bookingRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_REQUEST_NOT_FOUND));

        if (bookingRequest.getRequestType() != ActionType.CANCEL
                && bookingRequest.getRequestType() != ActionType.CHANGE) {
            throw new AppException(ErrorCode.ACTION_TYPE_INVALID);
        }

        if (bookingRequest.getStatus() != BookingStatus.PENDING_CANCELLATION
                && bookingRequest.getStatus() != BookingStatus.PENDING_CHANGE) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        User admin = userRepository.findById(adminId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        bookingRequest.setReviewedAt(LocalDateTime.now());
        bookingRequest.setAdmin(admin);
        bookingRequest.setStatus(request.getStatus());
        bookingRequestRepository.save(bookingRequest);

        // CONFIRMED_CANCELLATION
        if (request.getStatus() == BookingStatus.CONFIRMED_CANCELLATION) {
            if (bookingRequest.getRequestType() != ActionType.CANCEL) {
                throw new AppException(ErrorCode.ACTION_TYPE_INVALID);
            }

            Booking booking = bookingRequest.getBooking();
            booking.setBookingStatus(BookingStatus.CONFIRMED_CANCELLATION);
            bookingRepository.save(booking);

            setHistoryRepository(bookingRequest);
        } // CONFIRMED_CHANGE
        else if (request.getStatus() == BookingStatus.CONFIRMED_CHANGE) {
            if (bookingRequest.getRequestType() != ActionType.CHANGE) {
                throw new AppException(ErrorCode.ACTION_TYPE_INVALID);
            }

            if (bookingRequest.getNewTour() == null) {
                throw new AppException(ErrorCode.TOUR_NOT_FOUND);
            }

            Booking booking = bookingRequest.getBooking();
            booking.setBookingStatus(BookingStatus.CONFIRMED_CHANGE);
            booking.setTour(bookingRequest.getNewTour());
            bookingRepository.save(booking);

            setHistoryRepository(bookingRequest);
        }

        BookingRequestResponse response = BookingRequestResponse.builder()
                .requestId(bookingRequest.getRequestId())
                .reason(bookingRequest.getReason())
                .requestType(bookingRequest.getRequestType())
                .status(request.getStatus())
                .reviewedAt(bookingRequest.getReviewedAt())
                .createdAt(bookingRequest.getCreatedAt())
                .adminId(admin.getId())
                .bookingId(bookingRequest.getBooking().getBookingId())
                .newTourId(
                        bookingRequest.getNewTour() != null
                                ? bookingRequest.getNewTour().getTourId()
                                : null)
                .oldTourId(
                        bookingRequest.getOldTour() != null
                                ? bookingRequest.getOldTour().getTourId()
                                : null)
                .userId(bookingRequest.getUser().getId())
                .build();

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingRequestResponse updateBookingRequestStatusCustomer(
            String bookingId, String userId, BookingCancelUpdateRequest request) {

        Booking booking =
                bookingRepository.findById(bookingId).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        //        // Validate booking belongs to user
        //        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
        //            throw new AppException(ErrorCode.UNAUTHORIZED);
        //        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (booking.getTour() == null) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        BookingRequest bookingRequest = BookingRequest.builder()
                .createdAt(LocalDateTime.now())
                .reason(request.getReason())
                .requestType(ActionType.CANCEL)
                .status(BookingStatus.PENDING_CANCELLATION)
                .booking(booking)
                .oldTour(booking.getTour())
                .user(booking.getUser())
                .newTour(null)
                .admin(null)
                .reviewedAt(null)
                .build();

        bookingRequestRepository.save(bookingRequest);

        booking.setBookingStatus(BookingStatus.PENDING_CANCELLATION);
        bookingRepository.save(booking);

        // History update
        setHistoryRepository(bookingRequest);

        BookingRequestResponse response = BookingRequestResponse.builder()
                .requestId(bookingRequest.getRequestId())
                .reason(bookingRequest.getReason())
                .requestType(bookingRequest.getRequestType())
                .status(bookingRequest.getStatus())
                .reviewedAt(bookingRequest.getReviewedAt())
                .createdAt(bookingRequest.getCreatedAt())
                .adminId(
                        bookingRequest.getAdmin() != null
                                ? bookingRequest.getAdmin().getId()
                                : null)
                .bookingId(bookingRequest.getBooking().getBookingId())
                .newTourId(
                        bookingRequest.getNewTour() != null
                                ? bookingRequest.getNewTour().getTourId()
                                : null)
                .oldTourId(
                        bookingRequest.getOldTour() != null
                                ? bookingRequest.getOldTour().getTourId()
                                : null)
                .userId(
                        bookingRequest.getUser() != null
                                ? bookingRequest.getUser().getId()
                                : null)
                .build();

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void setHistoryRepository(BookingRequest bookingRequest) {
        History history = History.builder()
                .tourId(
                        bookingRequest.getOldTour() != null
                                ? bookingRequest.getOldTour().getTourId()
                                : bookingRequest.getBooking().getTour().getTourId())
                .actionType(ActionType.CANCEL)
                .timestamp(LocalDateTime.now())
                .build();

        // user != null ? setContact null : setUser null
        if (bookingRequest.getUser() != null) {
            history.setUser(bookingRequest.getUser());
            history.setContact(null);
        } else if (bookingRequest.getBooking().getContact() != null) {
            history.setUser(null);
            history.setContact(bookingRequest.getBooking().getContact());
        }
        historyRepository.save(history);
    }
}
