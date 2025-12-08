package tourbooking.vietvivu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.ActionType;
import tourbooking.vietvivu.enumm.BookingStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "booking_requests")
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id")
    String requestId;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "reason", columnDefinition = "text", nullable = false)
    String reason;

    @Column(name = "request_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ActionType requestType;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    User admin;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "new_tour_id")
    Tour newTour;

    @ManyToOne
    @JoinColumn(name = "old_tour_id", nullable = false)
    Tour oldTour;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    Promotion promotion;
}
