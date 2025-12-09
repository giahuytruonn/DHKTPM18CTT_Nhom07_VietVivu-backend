package tourbooking.vietvivu.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.hibernate.annotations.Formula;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.TourStatus;

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

    @NotBlank
    @Size(max = 200)
    String title;

    @Size(max = 2000)
    String description;

    @PositiveOrZero
    @Column(name = "initial_quantity")
    Integer initialQuantity;

    @NotNull
    @PositiveOrZero
    Integer quantity;

    @NotNull
    @PositiveOrZero
    @Column(name = "price_adult")
    Double priceAdult;

    @NotNull
    @PositiveOrZero
    @Column(name = "price_child")
    Double priceChild;

    @NotBlank
    // @Pattern(regexp = "^\\d+ ngày( \\d+ đêm)?$") // Đã comment ở lần trước
    String duration;

    @NotBlank
    @Size(max = 100)
    String destination;

    @NotNull
    Boolean availability;

    @NotNull
    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "tour_status")
    @Enumerated(EnumType.STRING)
    TourStatus tourStatus;

    //    @NotEmpty
    //    @Size(min = 1, max = 20)
    @ElementCollection
    @CollectionTable(name = "tour_itinerary", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "step")
    List<@Size(max = 500) String> itinerary;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Image> images;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.REMOVE)
    Set<Review> reviews;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.REMOVE)
    Set<Booking> bookings;

    @ManyToMany(mappedBy = "favoriteTours")
    private Set<User> usersFavorited = new HashSet<>();

    @Formula("(SELECT COUNT(*) FROM bookings b WHERE b.tour_id = tour_id)")
    Integer totalBookings;

    @Formula("(SELECT COUNT(*) FROM user_favorite uf WHERE uf.tour_id = tour_id)")
    Integer favoriteCount;
}
