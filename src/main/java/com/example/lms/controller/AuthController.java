package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.model.User;
import com.example.lms.service.AuthService;
import com.example.lms.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            AuthResponse res = authService.login(req);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
        try {
            User u = authService.register(r);
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return ResponseEntity.status(401).body("Invalid token");
        String token = authHeader.substring(7);
        User user = authService.getCurrentUser(token);
        if (user == null) return ResponseEntity.status(401).body("User not found");
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{staffId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String staffId) {
        try {
            User user = authService.getUserByStaffId(staffId);

            return ResponseEntity.ok(Map.of(
                    "CL", user.getClBalance(),
                    "EL", user.getElBalance(),
                    "ML", user.getMlBalance(),
                    "PL", user.getPlBalance(),
                    "SL", 0,               // optional — update later
                    "WITHOUT_PAY", 0       // optional — update later
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.createAndSendOtp(req.getEmail());
        // Always return success to avoid username enumeration.
        return ResponseEntity.ok().body(Map.of("message", "If an account with that email exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        boolean ok = passwordResetService.verifyOtpAndReset(req.getEmail(), req.getOtp(), req.getNewPassword());
        if (!ok) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP or expired or user not found"));
        }
        return ResponseEntity.ok().body(Map.of("message", "Password reset successful"));
    }


}
