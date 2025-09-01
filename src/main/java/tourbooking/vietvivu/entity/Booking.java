package tourbooking.vietvivu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    LocalDate bookingDate;

    @Column(name = "num_adults")
    Integer numAdults;

    @Column(name = "num_children")
    Integer numChildren;

    @Column(name = "total_price")
    Double totalPrice;

    @Column(name = "payment_status")
    String paymentStatus;

    @Column(name = "booking_status")
    String bookingStatus;

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

}
