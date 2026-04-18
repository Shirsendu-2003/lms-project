package com.example.lms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_otps", indexes = {
        @Index(columnList = "email")
})
@Getter
@Setter
public class PasswordResetOtp {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp; // could be hashed if you prefer

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetOtp() {}

    // getters & setters
    // (omitted for brevity)
}
