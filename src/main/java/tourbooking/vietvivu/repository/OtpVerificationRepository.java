package tourbooking.vietvivu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tourbooking.vietvivu.entity.OtpVerification;

import java.time.LocalDateTime;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {
    OtpVerification findOtpVerificationByOtp(String otp);
    void deleteByExpiredAtBefore(LocalDateTime time);
}
