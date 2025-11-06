package tourbooking.vietvivu.entity;

import java.io.Serializable;
import java.util.HashSet;
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
@Table(name = "contacts")
public class Contact implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contact_id")
    String id;

    String email;
    String name;
    String address;

    @Column(name = "phone_number")
    String phoneNumber;

    @OneToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<History> histories = new HashSet<>();
}
