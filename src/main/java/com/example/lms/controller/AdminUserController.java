package com.example.lms.controller;

import com.example.lms.dto.CreateUserRequest;
import com.example.lms.model.Department;
import com.example.lms.model.Role;
import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.EmailService;
import com.example.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostMapping("/user/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {

        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required");
        }

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        if (req.getRole() == null || req.getRole().isBlank()) {
            return ResponseEntity.badRequest().body("Role is required");
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().trim().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid role: " + req.getRole());
        }

        Department dept;
        try {
            dept = (req.getDepartment() == null || req.getDepartment().isBlank())
                    ? Department.OFFICE_STAFF
                    : Department.valueOf(req.getDepartment().trim().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid department: " + req.getDepartment());
        }

        String rawPassword = (req.getPassword() == null || req.getPassword().isBlank())
                ? "Temp@1234"
                : req.getPassword();

        String doj = (req.getDateOfJoining() == null || req.getDateOfJoining().isBlank())
                ? LocalDate.now().toString()
                : req.getDateOfJoining();

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .mobileNo(req.getMobileNo())
                .role(role)
                .department(dept)
                .password(passwordEncoder.encode(rawPassword))
                .dateOfJoining(doj)
                .active(true)
                .blocked(false)
                .lastUpdate(LocalDateTime.now())
                .clBalance(15)
                .plBalance(28)
                .mlBalance(10)
                .elBalance(28)
                .build();

        user.setStaffId(userService.generateStaffId(req.getName(), dept));

        User saved = userRepository.save(user);

        emailService.sendPlainEmail(
                saved.getEmail(),
                "LMS - Account Created",
                "Hello " + saved.getName() +
                        "\n\nStaff ID: " + saved.getStaffId() +
                        "\nPassword: " + rawPassword +
                        "\n\nPlease change your password after first login."
        );

        return ResponseEntity.ok(saved);
    }

}
