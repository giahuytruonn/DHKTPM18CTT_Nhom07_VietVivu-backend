package tourbooking.vietvivu.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class OtpService {
    public String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    public LocalDateTime getExpiredTime() {
        return LocalDateTime.now().plusMinutes(1);
    }
}
