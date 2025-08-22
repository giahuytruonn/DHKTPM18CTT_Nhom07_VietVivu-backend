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
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    String historyId;

    @Column(name = "tour_id")
    String tourId;

    @Column(name = "invalid_token")
    String invalidToken;

    @Column(name = "action_type")
    String actionType;

    LocalDate timestamp;

    @Column(name = "expiry_time")
    LocalDate expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
