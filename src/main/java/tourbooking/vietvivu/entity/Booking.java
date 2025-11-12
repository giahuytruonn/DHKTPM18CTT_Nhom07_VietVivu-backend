package tourbooking.vietvivu.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.BookingStatus;
import tourbooking.vietvivu.enumm.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    String bookingId;

    @Column(name = "booking_date")
    LocalDateTime bookingDate;

    @Column(name = "num_adults")
    Integer numAdults;

    @Column(name = "num_children")
    Integer numChildren;

    @Column(name = "total_price")
    Double totalPrice;

    String note;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus;

    @Column(name = "payment_term")
    LocalDateTime paymentTerm;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    Tour tour;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    Promotion promotion;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    Checkout checkout;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    Invoice invoice;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    Review review;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    Contact contact;
}
