package tourbooking.vietvivu.entity;

import jakarta.persistence.*;
import java.io.Serializable;
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
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    String id;

    String username;
    String password;
    String email;
    String name;
    String address;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "created_date")
    LocalDate createdDate;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    Set<History> histories;

    @OneToMany(mappedBy = "user")
    Set<Conversation> conversationsAsUser;

    @OneToMany(mappedBy = "admin")
    Set<Conversation> conversationsAsAdmin;

    @OneToMany(mappedBy = "sender")
    Set<Message> messages;

    @OneToMany(mappedBy = "user")
    Set<Review> reviews;

    @OneToMany(mappedBy = "user")
    Set<Booking> bookings;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    Set<InvalidatedToken> invalidatedTokens;
}
