package tourbooking.vietvivu.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.BookingRequestStatusUpdateRequest;
import tourbooking.vietvivu.dto.request.BookingStatusUpdateRequest;
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

    public List<BookingRequestResponse> getPendingRequests() {
        List<BookingStatus> pendingStatuses =
                Arrays.asList(BookingStatus.PENDING_CANCELLATION, BookingStatus.PENDING_CHANGE);
        List<BookingRequest> requests = bookingRequestRepository.findByStatusIn(pendingStatuses);
        return requests.stream().map(this::mapToResponse).toList();
    }

    public BookingRequestResponse getById(String requestId) {
        BookingRequest bookingRequest = bookingRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_REQUEST_NOT_FOUND));
        return mapToResponse(bookingRequest);
    }

    private BookingRequestResponse mapToResponse(BookingRequest bookingRequest) {
        return BookingRequestResponse.builder()
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
    }

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

            setHistoryRepository(bookingRequest, ActionType.CANCEL);
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

            setHistoryRepository(bookingRequest, ActionType.CHANGE);
        } // DENIED_CANCELLATION
        else if (request.getStatus() == BookingStatus.DENIED_CANCELLATION) {
            if (bookingRequest.getRequestType() != ActionType.CANCEL) {
                throw new AppException(ErrorCode.ACTION_TYPE_INVALID);
            }

            // Restore booking status to CONFIRMED
            Booking booking = bookingRequest.getBooking();
            booking.setBookingStatus(BookingStatus.DENIED_CANCELLATION);
            bookingRepository.save(booking);

            setHistoryRepository(bookingRequest, ActionType.CANCEL);
        } // DENIED_CHANGE
        else if (request.getStatus() == BookingStatus.DENIED_CHANGE) {
            if (bookingRequest.getRequestType() != ActionType.CHANGE) {
                throw new AppException(ErrorCode.ACTION_TYPE_INVALID);
            }

            Booking booking = bookingRequest.getBooking();
            booking.setBookingStatus(BookingStatus.DENIED_CHANGE);
            bookingRepository.save(booking);

            setHistoryRepository(bookingRequest, ActionType.CHANGE);
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
            String bookingId, String userId, BookingStatusUpdateRequest request) {

        Booking booking =
                bookingRepository.findById(bookingId).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Validate booking belongs to user
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.USER_NOT_BELONG_BOOKING);
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED
                && booking.getBookingStatus() != BookingStatus.DENIED_CANCELLATION
                && booking.getBookingStatus() != BookingStatus.DENIED_CHANGE) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        if (booking.getTour() == null) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        // Validate reason is not empty
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        BookingRequest bookingRequest;
        String newTourId = request.getNewTourId();

        if (newTourId != null && !newTourId.trim().isEmpty()) {
            Tour newTour = tourRepository
                    .findById(newTourId)
                    .orElseThrow(() -> new AppException(ErrorCode.NEW_TOUR_NOT_FOUND));

            // Validate cannot change to the same tour
            if (newTour.getTourId().equals(booking.getTour().getTourId())) {
                throw new AppException(ErrorCode.CANNOT_CHANGE_TO_SAME_TOUR);
            }

            bookingRequest = BookingRequest.builder()
                    .createdAt(LocalDateTime.now())
                    .reason(request.getReason())
                    .requestType(ActionType.CHANGE)
                    .status(BookingStatus.PENDING_CHANGE)
                    .booking(booking)
                    .oldTour(booking.getTour())
                    .user(booking.getUser())
                    .newTour(newTour)
                    .admin(null)
                    .reviewedAt(null)
                    .build();

            bookingRequestRepository.save(bookingRequest);
            booking.setBookingStatus(BookingStatus.PENDING_CHANGE);
            bookingRepository.save(booking);
            // History update
            setHistoryRepository(bookingRequest, ActionType.CHANGE);

        } else {
            bookingRequest = BookingRequest.builder()
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
            setHistoryRepository(bookingRequest, ActionType.CANCEL);
        }

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
    public void setHistoryRepository(BookingRequest bookingRequest, ActionType actionType) {
        History history = History.builder()
                .tourId(
                        bookingRequest.getOldTour() != null
                                ? bookingRequest.getOldTour().getTourId()
                                : bookingRequest.getBooking().getTour().getTourId())
                .actionType(actionType)
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
