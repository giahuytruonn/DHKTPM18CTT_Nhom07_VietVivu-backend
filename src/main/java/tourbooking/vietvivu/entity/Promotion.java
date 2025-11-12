package tourbooking.vietvivu.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @Column(name = "promotion_id")
    String promotionId;

    String description;
    Double discount;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    Boolean status;
    Integer quantity;

    @OneToMany(mappedBy = "promotion")
    Set<Booking> bookings;
}
