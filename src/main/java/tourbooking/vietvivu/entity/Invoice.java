package tourbooking.vietvivu.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id")
    String invoiceId;

    Double amount;

    @Column(name = "date_issued")
    LocalDate dateIssued;

    String details;

    @OneToOne
    @JoinColumn(name = "checkout_id")
    Checkout checkout;

    @OneToOne
    @JoinColumn(name = "booking_id")
    Booking booking;
}
