package com.example.lms.service;

import com.example.lms.model.PasswordResetOtp;
import com.example.lms.repository.PasswordResetOtpRepository;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetOtpRepository otpRepo;
    private final EmailService emailService;
    private final UserRepository userRepository;// your existing user repo
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expire-minutes:10}")
    private long expireMinutes;

    @Value("${app.otp.code-length:6}")
    private int codeLength;

    private String generateOtp() {
        Random rnd = new Random();
        int bound = (int) Math.pow(10, codeLength - 1);
        int number = bound + rnd.nextInt(9 * bound);
        return String.valueOf(number);
    }

    @Transactional
    public void createAndSendOtp(String email) {
        // Optionally verify user exists
        if (!userRepository.existsByEmail(email)) {
            // For security, do not reveal; just return silently or still send success (choose policy)
            // We'll still create OTP so user receives email if account exists.
        }

        PasswordResetOtp otpEntity = new PasswordResetOtp();
        otpEntity.setEmail(email);
        String otp = generateOtp();
        otpEntity.setOtp(otp);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));
        otpEntity.setUsed(false);
        otpRepo.save(otpEntity);

        emailService.sendOtpEmail(email, otp);
    }

    @Transactional
    public boolean verifyOtpAndReset(String email, String otp, String newPassword) {
        var maybe = otpRepo.findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email);
        if (maybe.isEmpty()) return false;
        PasswordResetOtp found = maybe.get();

        if (found.isUsed()) return false;
        if (found.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        if (!found.getOtp().equals(otp)) return false;

        // mark used
        found.setUsed(true);
        otpRepo.save(found);

        // find user and change password
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        var user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    // scheduled cleanup can call otpRepo.deleteByExpiresAtBefore(LocalDateTime.now());
}
