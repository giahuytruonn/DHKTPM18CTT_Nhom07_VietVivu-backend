package tourbooking.vietvivu.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
@Table(name = "tours")
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tour_id")
    String tourId;

    String title;
    String description;
    Integer quantity;

    @Column(name = "price_adult")
    Double priceAdult;

    @Column(name = "price_child")
    Double priceChild;

    String duration;
    String destination;
    Boolean availability;

    @Column(name = "start_date")
    LocalDate startDate;

    @ElementCollection
    @CollectionTable(name = "tour_itinerary", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "step")
    List<String> itinerary;

    @OneToMany(mappedBy = "tour")
    Set<Image> images;

    @OneToMany(mappedBy = "tour")
    Set<Review> reviews;

    @OneToMany(mappedBy = "tour")
    Set<Booking> bookings;
}
