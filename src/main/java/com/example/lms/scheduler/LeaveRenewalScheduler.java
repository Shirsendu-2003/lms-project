package com.example.lms.scheduler;

import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveRenewalScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 1 1 *")
    @Transactional
    public void renewLeavesYearly() {

        log.info("✅ Running yearly leave renewal...");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            user.setClBalance(15);
            user.setMlBalance(10);
            user.setPlBalance(user.getPlBalance() + 28);
            user.setElBalance(user.getElBalance() + 28);

            userRepository.save(user);
        }

        log.info("✅ Leave renewal completed for {} users", users.size());
    }
}
