package tourbooking.vietvivu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tourbooking.vietvivu.enumm.ActionType;

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
    @Enumerated(EnumType.STRING)
    ActionType actionType;

    LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    Contact contact;
}
