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
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    String historyId;

    @Column(name = "tour_id")
    String tourId;

    @Column(name = "action_type")
    String actionType;

    LocalDate timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
