package com.example.lms.repository;

import com.example.lms.model.Department;
import com.example.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStaffId(String staffId);
    boolean existsByEmail(String email);

    // ✅ FIXED: Enum-based Query
    List<User> findByDepartment(Department department);
}
