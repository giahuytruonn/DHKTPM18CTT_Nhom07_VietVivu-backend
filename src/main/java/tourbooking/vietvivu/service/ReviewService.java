package tourbooking.vietvivu.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tourbooking.vietvivu.dto.request.ReviewRequest;
import tourbooking.vietvivu.dto.response.ReviewResponse;
import tourbooking.vietvivu.entity.Booking;
import tourbooking.vietvivu.entity.Review;
import tourbooking.vietvivu.entity.Tour;
import tourbooking.vietvivu.entity.User;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.exception.AppException;
import tourbooking.vietvivu.exception.ErrorCode;
import tourbooking.vietvivu.repository.BookingRepository;
import tourbooking.vietvivu.repository.ReviewRepository;
import tourbooking.vietvivu.repository.TourRepository;
import tourbooking.vietvivu.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    ReviewRepository reviewRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;
    TourRepository tourRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Booking booking = bookingRepository
                .findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra user có thuộc booking này không
        if (booking.getUser() == null || !booking.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.USER_NOT_BELONG_BOOKING);
        }

        // Kiểm tra booking đã hoàn thành chưa
        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.BOOKING_NOT_COMPLETED);
        }

        // Kiểm tra đã có review cho booking này chưa
        if (booking.getReview() != null) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Tour tour = booking.getTour();
        if (tour == null) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .timestamp(LocalDate.now())
                .user(user)
                .tour(tour)
                .booking(booking)
                .build();

        Review savedReview = reviewRepository.save(review);
        booking.setReview(savedReview);
        bookingRepository.save(booking);

        return mapToReviewResponse(savedReview);
    }

    public ReviewResponse getReview(String reviewId) {
        Review review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        return mapToReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByTour(String tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        return tour.getReviews().stream().map(this::mapToReviewResponse).collect(Collectors.toList());
    }

    public List<ReviewResponse> getMyReviews() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return user.getReviews().stream().map(this::mapToReviewResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse updateReview(String reviewId, ReviewRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Review review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // Kiểm tra user có quyền sửa review này không
        if (!review.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setTimestamp(LocalDate.now());

        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(String reviewId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Review review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // Kiểm tra user có quyền xóa review này không
        if (!review.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xóa liên kết với booking
        if (review.getBooking() != null) {
            review.getBooking().setReview(null);
            bookingRepository.save(review.getBooking());
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .comment(review.getComment())
                .timestamp(review.getTimestamp())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : null)
                .tourId(review.getTour() != null ? review.getTour().getTourId() : null)
                .tourTitle(review.getTour() != null ? review.getTour().getTitle() : null)
                .bookingId(review.getBooking() != null ? review.getBooking().getBookingId() : null)
                .build();
    }
}
