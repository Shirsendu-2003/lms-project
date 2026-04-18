package com.example.lms.service;

import com.example.lms.model.ActivityLog;
import com.example.lms.model.Department;
import com.example.lms.model.User;
import com.example.lms.repository.ActivityLogRepository;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final ActivityLogRepository logRepo;

    public List<User> findAll() { return userRepo.findAll(); }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<ActivityLog> getAllLogs() {
        return logRepo.findAllByOrderByTimeDesc();
    }

    public Optional<User> findByEmail(String email) { return userRepo.findByEmail(email); }

    public Optional<User> findByStaffId(String staffId) { return userRepo.findByStaffId(staffId); }

    public User save(User u) { return userRepo.save(u); }

    @Transactional
    public String generateStaffId(String fullName, Department dept) {
        String initials = getInitialsFromName(fullName);
        String prefix = initials + dept.name();

        List<User> all = userRepo.findAll();
        Pattern p = Pattern.compile("^" + Pattern.quote(prefix) + "(\\d{3})$");

        int max = all.stream()
                .map(User::getStaffId)
                .filter(s -> s != null)
                .map(s -> {
                    Matcher m = p.matcher(s);
                    if (m.find()) return Integer.parseInt(m.group(1));
                    return 0;
                })
                .max(Integer::compare)
                .orElse(0);

        return String.format("%s%03d", prefix, max + 1);
    }

    private String getInitialsFromName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "XX";

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0];
        String last = parts.length > 1 ? parts[parts.length - 1] : "";

        char a = Character.toUpperCase(first.charAt(0));
        char b = last.isEmpty() ? 'X' : Character.toUpperCase(last.charAt(0));

        return "" + a + b;
    }


    @Transactional
    public void toggleIncharge(UUID staffId, boolean status, String actor) {

        // 🔥 Step 1: Remove in-charge from all other users
        if (status) {
            List<User> allUsers = userRepo.findAll();
            for (User u : allUsers) {
                if (u.isIncharge() && !u.getId().equals(staffId)) {
                    u.setIncharge(false);
                    userRepo.save(u);
                }
            }
        }

        // Step 2: Set in-charge for the selected user
        User user = userRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIncharge(status);
        userRepo.save(user);

        // Step 3: Save activity log
        ActivityLog log = ActivityLog.builder()
                .actor(actor)
                .action(status ? "set as in-charge" : "removed from in-charge")
                .target(user.getName() + " (" + user.getRole() + ")")
                .time(LocalDateTime.now())
                .build();

        logRepo.save(log);
    }

}
