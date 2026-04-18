package com.example.lms.controller;

import com.example.lms.dto.ApplicationRequest;
import com.example.lms.dto.ApplicationResponse;
import com.example.lms.dto.CreateUserRequest;
import com.example.lms.model.Department;
import com.example.lms.model.Role;
import com.example.lms.model.User;
import com.example.lms.service.ApplicationService;
import com.example.lms.service.EmailService;
import com.example.lms.service.UserService;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ApplicationService applicationService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(userService.findAll());
    }




    // ✅ 2. Delete User
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("User not found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    // ✅ 3. Toggle Active / Blocked
    @PutMapping("/users/toggle-status/{id}")
    public ResponseEntity<?> toggleActive(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        return ResponseEntity.ok("Status updated");
    }

    // ✅ 4. Get user by ID (for edit page)
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID id,
            @RequestBody User updatedUser
    ) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // update allowed fields
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setMobileNo(updatedUser.getMobileNo());
        user.setDepartment(updatedUser.getDepartment());
        user.setRole(updatedUser.getRole());

        userRepository.save(user);

        return ResponseEntity.ok("User updated");
    }


    @GetMapping("/leaves")
    public List<ApplicationResponse> getAllLeaves() {
        return applicationService.getAllApplications();
    }



}
