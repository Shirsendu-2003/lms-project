package com.example.lms.task;

import com.example.lms.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpCleanupTask {

    private final PasswordResetOtpRepository otpRepo;

    // runs every hour
    @Transactional     // <-- REQUIRED FIX
    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        otpRepo.deleteByExpiresAtBefore(LocalDateTime.now().minusMinutes(1));
    }
}
