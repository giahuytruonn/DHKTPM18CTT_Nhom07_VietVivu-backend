package tourbooking.vietvivu.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Tiêu đề tour không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    String title;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    String description;

    @PositiveOrZero(message = "Số lượng ban đầu phải ≥ 0")
    @Column(name = "initial_quantity", nullable = true)
    Integer initialQuantity;

    @NotNull(message = "Số lượng còn lại không được để trống")
    @PositiveOrZero(message = "Số lượng còn lại phải ≥ 0")
    Integer quantity;

    @NotNull(message = "Giá người lớn không được để trống")
    @PositiveOrZero(message = "Giá người lớn phải ≥ 0")
    @Column(name = "price_adult")
    Double priceAdult;

    @NotNull(message = "Giá trẻ em không được để trống")
    @PositiveOrZero(message = "Giá trẻ em phải ≥ 0")
    @Column(name = "price_child")
    Double priceChild;

    @NotBlank(message = "Thời gian tour không được để trống")
    @Pattern(regexp = "^\\d+ ngày( \\d+ đêm)?$", message = "Thời gian phải có định dạng: 'X ngày' hoặc 'X ngày Y đêm'")
    String duration;

    @NotBlank(message = "Điểm đến không được để trống")
    @Size(max = 100, message = "Điểm đến tối đa 100 ký tự")
    String destination;

    @NotNull(message = "Trạng thái khả dụng không được để trống")
    Boolean availability;

    @NotNull(message = "Ngày khởi hành không được để trống")
    @FutureOrPresent(message = "Ngày khởi hành phải từ hôm nay trở đi")
    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    LocalDate endDate;

    @Column(name = "tour_status")
    @Enumerated(EnumType.STRING)
    TourStatus tourStatus;

    @NotEmpty(message = "Lịch trình không được để trống")
    @Size(min = 1, max = 20, message = "Lịch trình phải có từ 1 đến 20 bước")
    @ElementCollection
    @CollectionTable(name = "tour_itinerary", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "step")
    List<@Size(max = 500, message = "Mỗi bước tối đa 500 ký tự") String> itinerary;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Image> images;

    @OneToMany(mappedBy = "tour")
    Set<Review> reviews;

    @OneToMany(mappedBy = "tour")
    Set<Booking> bookings;

    @ManyToMany(mappedBy = "favoriteTours")
    private Set<User> usersFavorited = new HashSet<>();
}