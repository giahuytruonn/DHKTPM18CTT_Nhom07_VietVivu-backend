package tourbooking.vietvivu.entity;

import java.time.LocalDateTime;

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
@Table(name = "explore_videos")
public class ExploreVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "video_id")
    String id;

    String title;

    String description;

    @Column(name = "video_url")
    String videoUrl;

    @Column(name = "uploaded_at")
    LocalDateTime uploadedAt;

    Boolean approved;
}
