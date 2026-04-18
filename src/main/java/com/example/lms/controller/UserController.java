package com.example.lms.controller;

import com.example.lms.model.Department;
import com.example.lms.model.User;
import com.example.lms.model.ActivityLog;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // Get all users
    @GetMapping("/users/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Toggle in-charge status
    @PutMapping("/users/incharge/{id}")
    public void toggleIncharge(
            @PathVariable UUID id,
            @RequestParam boolean status,
            @RequestHeader(value = "actor", defaultValue = "Admin") String actor
    ) {
        userService.toggleIncharge(id, status, actor);
    }

    // Get all activity logs
    @GetMapping("/logs/incharge")
    public List<ActivityLog> getLogs() {
        return userService.getAllLogs();
    }
    @GetMapping("/users/by-department")
    public List<User> getUsersByDepartment(@RequestParam String dept) {

        Department department = Department.valueOf(dept); // ✅ Convert String → Enum

        return userRepository.findByDepartment(department);
    }

}
