package com.example.lms.config;

import com.example.lms.model.Department;
import com.example.lms.model.Role;
import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ✅ Insert default ADMIN only if not exists
        if (userRepository.findByEmail("admin@lms.com").isEmpty()) {

            User admin = User.builder()
                    .staffId("SPADMIN001")
                    .name("System Admin")
                    .email("admin@lms.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .department(Department.ADMINISTRATION)
                    .mobileNo("9999999999")
                    .dateOfJoining("2024-01-01")
                    .active(true)
                    .blocked(false)
                    .clBalance(15)
                    .plBalance(28)
                    .elBalance(28)
                    .mlBalance(10)
                    .build();

            userRepository.save(admin);

            System.out.println("✅ Default admin created: admin@lms.com / admin123");
        } else {
            System.out.println("✅ Admin already exists, skipping...");
        }



    }
}
