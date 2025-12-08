package tourbooking.vietvivu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "otp_verification")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OtpVerification {

    @Id
    private String email;

    private String otp;
    private LocalDateTime expiredAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiredAt;
}
