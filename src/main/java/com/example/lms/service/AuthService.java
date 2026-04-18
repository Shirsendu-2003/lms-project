package com.example.lms.service;

import com.example.lms.dto.AuthRequest;
import com.example.lms.dto.AuthResponse;
import com.example.lms.dto.LeaveBalanceResponse;
import com.example.lms.dto.RegisterRequest;
import com.example.lms.model.*;
import com.example.lms.repository.UserRepository;
import com.example.lms.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(AuthRequest req) {
        User user = null;

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            user = userRepository.findByEmail(req.getEmail()).orElse(null);
        }

        if (user == null && req.getStaffId() != null && !req.getStaffId().isEmpty()) {
            user = userRepository.findByStaffId(req.getStaffId()).orElse(null);
        }

        if (user == null) throw new RuntimeException("User not found!");

        if (!user.isActive()) {
            throw new RuntimeException("Your account is blocked by admin");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password!");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        user.setLastUpdate(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(token, user);
    }

    public User register(RegisterRequest r) {
        if (userRepository.existsByEmail(r.getEmail()))
            throw new RuntimeException("Email already exists!");

        Role role = Role.valueOf(r.getRole().trim().toUpperCase());
        Department dept = Department.valueOf(r.getDepartment().trim().toUpperCase());

        String doj = (r.getDateOfJoining() == null || r.getDateOfJoining().isEmpty())
                ? String.valueOf(LocalDate.now()) : r.getDateOfJoining();

        User user = User.builder()
                .name(r.getName())
                .email(r.getEmail())
                .password(passwordEncoder.encode(r.getPassword()))
                .staffId(r.getStaffId())
                .department(dept)
                .role(role)
                .mobileNo(r.getMobileNo())
                .dateOfJoining(doj)
                .active(true)
                .lastUpdate(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public User getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) return null;
        String email = jwtUtil.extractEmail(token);
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    // ✅ Find user by staffId
    public User getUserByStaffId(String staffId) {
        return userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    // ✅ Deduct leave after application approval (optional)
    public void deductLeave(User user, String type, int days) {
        switch (type) {
            case "CL":
                user.setClBalance(user.getClBalance() - days);
                break;
            case "PL":
                user.setPlBalance(user.getPlBalance() - days);
                break;
            case "ML":
                user.setMlBalance(user.getMlBalance() - days);
                break;
            case "EL":
                user.setElBalance(user.getElBalance() - days);
                break;
        }
        userRepository.save(user);
    }



}
