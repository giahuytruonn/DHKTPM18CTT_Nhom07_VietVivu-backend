package tourbooking.vietvivu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tourbooking.vietvivu.repository.OtpVerificationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpCleanupService {

    private final OtpVerificationRepository otpRepo;

    @Transactional
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanExpiredOtp() {
        otpRepo.deleteByExpiredAtBefore(LocalDateTime.now());
    }
}
