package tourbooking.vietvivu.entity;

import java.time.LocalDate;
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
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String conversationId;

    @Column(name = "reply_status")
    Boolean replyStatus;

    @Column(name = "created_date")
    LocalDate createdDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    User admin;

    @OneToMany(mappedBy = "conversation")
    Set<Message> messages;
}
