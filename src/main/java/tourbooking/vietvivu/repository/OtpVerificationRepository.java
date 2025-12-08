package tourbooking.vietvivu.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import tourbooking.vietvivu.entity.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {
    OtpVerification findOtpVerificationByOtp(String otp);

    void deleteByExpiredAtBefore(LocalDateTime time);
}
