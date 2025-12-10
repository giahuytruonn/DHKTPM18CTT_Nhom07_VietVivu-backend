package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.response.ContactResponse;
import tourbooking.vietvivu.dto.response.PaginationResponse;
import tourbooking.vietvivu.entity.*;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final HistoryRepository historyRepository;
    private final EmailService emailService;

    public PaginationResponse<ContactResponse> getAllContacts(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("booking.bookingDate").descending());
        Page<Contact> contactPage = contactRepository.findAll(pageable);

        var contacts =
                contactPage.getContent().stream().map(this::mapToResponse).toList();

        return PaginationResponse.<ContactResponse>builder()
                .items(contacts)
                .currentPage(contactPage.getNumber())
                .pageSizes(contactPage.getSize())
                .totalItems((int) contactPage.getTotalElements())
                .totalPages(contactPage.getTotalPages())
                .build();
    }

    public ContactResponse getContactById(String contactId) {
        Contact contact =
                contactRepository.findById(contactId).orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));
        return mapToResponse(contact);
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactResponse cancelBookingByContact(String contactId, String reason) {
        Contact contact =
                contactRepository.findById(contactId).orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));

        Booking booking = contact.getBooking();
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // Validate booking can be cancelled
        BookingStatus currentStatus = booking.getBookingStatus();
        if (currentStatus == BookingStatus.CANCELLED
                || currentStatus == BookingStatus.CONFIRMED_CANCELLATION
                || currentStatus == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_STATUS_INVALID);
        }

        // Update booking status
        booking.setBookingStatus(BookingStatus.CONFIRMED_CANCELLATION);

        // Restore tour capacity
        restoreTourCapacity(booking);

        bookingRepository.save(booking);

        // Save history
        saveHistory(contact, booking, ActionType.CANCEL);

        // Create a transient BookingRequest (not saved) for email template
        BookingRequest emailData = BookingRequest.builder()
                .createdAt(LocalDateTime.now())
                .reason(reason != null ? reason : "Hủy bởi Admin")
                .requestType(ActionType.CANCEL)
                .status(BookingStatus.CONFIRMED_CANCELLATION)
                .booking(booking)
                .oldTour(booking.getTour())
                .build();

        // Send email notification
        sendCancellationEmail(contact, booking, emailData);

        return mapToResponse(contact);
    }

    private void restoreTourCapacity(Booking booking) {
        Tour tour = booking.getTour();
        if (tour == null) {
            log.warn("Cannot restore capacity because tour is null");
            return;
        }

        int adults = booking.getNumAdults() != null ? booking.getNumAdults() : 0;
        int children = booking.getNumChildren() != null ? booking.getNumChildren() : 0;
        int total = adults + children;

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
        log.info("Restored {} slots to tour {}", total, tour.getTourId());
    }

    private void saveHistory(Contact contact, Booking booking, ActionType actionType) {
        History history = History.builder()
                .tourId(booking.getTour() != null ? booking.getTour().getTourId() : null)
                .actionType(actionType)
                .timestamp(LocalDateTime.now())
                .user(null)
                .contact(contact)
                .build();
        historyRepository.save(history);
    }

    private void sendCancellationEmail(Contact contact, Booking booking, BookingRequest bookingRequest) {
        String recipient = contact.getEmail();
        if (recipient == null || recipient.isBlank()) {
            log.warn("Skip sending email because contact email is missing. Contact {}", contact.getId());
            return;
        }

        try {
            double penaltyRate = resolvePenaltyRate(booking);
            double refundAmount = calculateRefundAmount(booking, penaltyRate);

            emailService.sendBookingStatusNotification(
                    booking, bookingRequest, BookingStatus.CONFIRMED_CANCELLATION, penaltyRate, refundAmount);
        } catch (Exception ex) {
            log.warn("Could not send cancellation email to {}: {}", recipient, ex.getMessage());
        }
    }

    private double resolvePenaltyRate(Booking booking) {
        Tour tour = booking.getTour();
        LocalDate startDate = tour != null ? tour.getStartDate() : null;

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

    private ContactResponse mapToResponse(Contact contact) {
        Booking booking = contact.getBooking();
        Tour tour = booking != null ? booking.getTour() : null;

        return ContactResponse.builder()
                .contactId(contact.getId())
                .email(contact.getEmail())
                .name(contact.getName())
                .address(contact.getAddress())
                .phoneNumber(contact.getPhoneNumber())
                .bookingId(booking != null ? booking.getBookingId() : null)
                .bookingStatus(
                        booking != null && booking.getBookingStatus() != null
                                ? booking.getBookingStatus().name()
                                : null)
                .bookingDate(booking != null ? booking.getBookingDate() : null)
                .totalPrice(booking != null ? booking.getTotalPrice() : null)
                .numAdults(booking != null ? booking.getNumAdults() : null)
                .numChildren(booking != null ? booking.getNumChildren() : null)
                .tourId(tour != null ? tour.getTourId() : null)
                .tourTitle(tour != null ? tour.getTitle() : null)
                .tourDestination(tour != null ? tour.getDestination() : null)
                .build();
    }
}
