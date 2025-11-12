package tourbooking.vietvivu.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.PaymentMethod;
import tourbooking.vietvivu.enumm.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "checkout")
public class Checkout {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "checkout_id")
    String checkoutId;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    @Column(name = "payment_date")
    LocalDate paymentDate;

    Double amount;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Column(name = "transaction_id")
    String transactionId;

    @OneToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @OneToOne(mappedBy = "checkout", cascade = CascadeType.ALL)
    Invoice invoice;
}
