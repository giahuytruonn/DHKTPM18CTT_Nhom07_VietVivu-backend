package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HistoryRepository historyRepository;
    private final TourRepository tourRepository;
    private final PromotionRepository promotionRepository;
    private final EmailService emailService;

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
                .promotionId(
                        bookingRequest.getPromotion() != null
                                ? bookingRequest.getPromotion().getPromotionId()
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
            restoreTourCapacity(booking);
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
            Tour tourToRestore = bookingRequest.getOldTour() != null ? bookingRequest.getOldTour() : booking.getTour();
            restoreTourCapacity(tourToRestore, booking.getNumAdults(), booking.getNumChildren());
            booking.setBookingStatus(BookingStatus.CONFIRMED_CHANGE);
            booking.setTour(bookingRequest.getNewTour());
            if (bookingRequest.getPromotion() != null) {
                booking.setPromotion(bookingRequest.getPromotion());
            }
            // Recalculate total price based on new tour and applied promotion (if any)
            Double recalculatedTotal =
                    calculateTotalPrice(bookingRequest.getNewTour(), booking, bookingRequest.getPromotion());
            if (recalculatedTotal != null) {
                booking.setTotalPrice(recalculatedTotal);
            }
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

        notifyCustomer(bookingRequest, request.getStatus());

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

    private Double calculateTotalPrice(Tour tour, Booking booking, Promotion promotion) {
        if (tour == null || booking == null) {
            return null;
        }
        double adults = booking.getNumAdults() != null ? booking.getNumAdults() : 0;
        double children = booking.getNumChildren() != null ? booking.getNumChildren() : 0;
        double base = adults * (tour.getPriceAdult() != null ? tour.getPriceAdult() : 0)
                + children * (tour.getPriceChild() != null ? tour.getPriceChild() : 0);
        double discount = 0;
        if (promotion != null && promotion.getDiscount() != null) {
            discount = promotion.getDiscount();
        }
        return Math.max(base - discount, 0);
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
        Promotion promotion = null;

        if (newTourId != null && !newTourId.trim().isEmpty()) {
            Tour newTour = tourRepository
                    .findById(newTourId)
                    .orElseThrow(() -> new AppException(ErrorCode.NEW_TOUR_NOT_FOUND));

            // Validate cannot change to the same tour
            if (newTour.getTourId().equals(booking.getTour().getTourId())) {
                throw new AppException(ErrorCode.CANNOT_CHANGE_TO_SAME_TOUR);
            }

            promotion = consumePromotionIfPresent(request.getPromotionId());

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
                    .promotion(promotion)
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
                    .promotion(null)
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

    private void notifyCustomer(BookingRequest bookingRequest, BookingStatus status) {
        if (!shouldNotify(status)) {
            return;
        }

        try {
            double penaltyRate = resolvePenaltyRate(bookingRequest);
            double refundAmount = calculateRefundAmount(bookingRequest.getBooking(), penaltyRate);
            emailService.sendBookingStatusNotification(
                    bookingRequest.getBooking(), bookingRequest, status, penaltyRate, refundAmount);
        } catch (Exception ex) {
            log.warn(
                    "Không thể gửi email cập nhật trạng thái booking {}: {}",
                    bookingRequest.getBooking().getBookingId(),
                    ex.getMessage());
        }
    }

    private boolean shouldNotify(BookingStatus status) {
        return status == BookingStatus.CONFIRMED_CANCELLATION
                || status == BookingStatus.CONFIRMED_CHANGE
                || status == BookingStatus.DENIED_CANCELLATION
                || status == BookingStatus.DENIED_CHANGE;
    }

    private double resolvePenaltyRate(BookingRequest bookingRequest) {
        Tour referenceTour = bookingRequest.getOldTour() != null
                ? bookingRequest.getOldTour()
                : bookingRequest.getBooking().getTour();
        LocalDate startDate = referenceTour != null ? referenceTour.getStartDate() : null;
        if (startDate == null) {
            return 0.25d;
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        if (days > 15) {
            return 0.25d;
        }
        if (days >= 8) {
            return 0.5d;
        }
        return 1d;
    }

    private double calculateRefundAmount(Booking booking, double penaltyRate) {
        double total = booking.getTotalPrice() != null ? booking.getTotalPrice() : 0d;
        return Math.max(total - (total * penaltyRate), 0d);
    }

    private Promotion consumePromotionIfPresent(String promotionId) {
        if (promotionId == null || promotionId.trim().isEmpty()) {
            return null;
        }

        Promotion promotion = promotionRepository
                .findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        LocalDate today = LocalDate.now();
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(today)) {
            throw new AppException(ErrorCode.PROMOTION_EXPIRED);
        }
        if (!Boolean.TRUE.equals(promotion.getStatus())) {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }
        if (promotion.getQuantity() == null || promotion.getQuantity() <= 0) {
            throw new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE);
        }

        promotion.setQuantity(promotion.getQuantity() - 1);
        if (promotion.getQuantity() != null && promotion.getQuantity() <= 0) {
            promotion.setStatus(false);
        }
        promotionRepository.save(promotion);
        return promotion;
    }

    private void restoreTourCapacity(Booking booking) {
        restoreTourCapacity(booking.getTour(), booking.getNumAdults(), booking.getNumChildren());
    }

    private void restoreTourCapacity(Tour tour, Integer adults, Integer children) {
        if (tour == null) {
            log.warn("Cannot restore capacity because tour is null");
            return;
        }
        int total = (adults != null ? adults : 0) + (children != null ? children : 0);
        if (total <= 0) {
            return;
        }
        Integer initialQuantity = tour.getInitialQuantity();
        int updatedQuantity = tour.getQuantity() + total;
        if (initialQuantity != null) {
            updatedQuantity = Math.min(updatedQuantity, initialQuantity);
        }
        tour.setQuantity(updatedQuantity);
        if (updatedQuantity > 0) {
            tour.setAvailability(true);
        }
        tourRepository.save(tour);
    }
}
