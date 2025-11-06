package tourbooking.vietvivu.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tourbooking.vietvivu.dto.request.BookingRequestStatusUpdateRequest;
import tourbooking.vietvivu.dto.response.BookingRequestResponse;
import tourbooking.vietvivu.entity.BookingRequest;
import tourbooking.vietvivu.entity.History;
import tourbooking.vietvivu.entity.User;
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
    private final ContactRepository contactRepository;

    @Transactional(rollbackFor = Exception.class)
    public BookingRequestResponse updateBookingRequestStatus(
            String requestId, String adminId, BookingRequestStatusUpdateRequest request) {

        BookingRequest bookingRequest = bookingRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_REQUEST_NOT_FOUND));

        // validate
        if (bookingRequest.getRequestType() == ActionType.CANCEL) {
            if (bookingRequest.getStatus() != BookingStatus.PENDING)
                throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        } else {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        User admin = userRepository.findById(adminId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        bookingRequest.setReviewedAt(LocalDateTime.now());
        bookingRequest.setAdmin(admin);
        bookingRequest.setStatus(request.getStatus());
        bookingRequestRepository.save(bookingRequest);

        // History
        if (request.getStatus() == BookingStatus.CONFIRMED) {
            History history = History.builder()
                    .tourId(bookingRequest.getOldTour().getTourId())
                    .actionType(ActionType.CANCEL)
                    .timestamp(LocalDateTime.now())
                    .build();

            if (bookingRequest.getUser() != null) {
                history.setUser(bookingRequest.getUser());
                history.setContact(null);
            } else if (bookingRequest.getBooking().getContact() != null) {
                history.setUser(null);
                history.setContact(bookingRequest.getBooking().getContact());
            }
            historyRepository.save(history);
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
                .newTourId(bookingRequest.getNewTour().getTourId())
                .oldTourId(bookingRequest.getOldTour().getTourId())
                .userId(bookingRequest.getUser().getId())
                .build();

        return response;
    }
}
