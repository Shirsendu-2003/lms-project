package com.example.lms.repository;

import com.example.lms.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, String> {
    Optional<PasswordResetOtp> findTopByEmailAndUsedFalseOrderByExpiresAtDesc(String email);
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime time);
}
