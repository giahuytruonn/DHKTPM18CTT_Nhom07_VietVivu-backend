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
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id")
    String imageId;

    @Column(name = "image_url")
    String imageUrl;

    String description;

    @Column(name = "upload_date")
    LocalDate uploadDate;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    Tour tour;
}
