package com.example.lms.service;

import com.example.lms.dto.HODInchargeRequest;
import com.example.lms.model.HODIncharge;
import com.example.lms.repository.HODInchargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HODInchargeService {

    private final HODInchargeRepository repo;

    // Save / Update Incharge
    public HODIncharge save(HODInchargeRequest req) {

        // Step 1: Mark previous incharge as inactive
        repo.findByDepartmentAndActiveTrue(req.getDept())
                .ifPresent(old -> {
                    old.setActive(false);
                    repo.save(old);
                });

        // Step 2: Insert new incharge record (active = true)
        HODIncharge newInc = new HODIncharge();
        newInc.setDepartment(req.getDept());
        newInc.setStaffId(req.getStaffId());
        newInc.setAssignedBy("HOD");   // Optional: replace with logged user
        newInc.setAssignedAt(LocalDateTime.now());
        newInc.setActive(true);

        return repo.save(newInc);
    }

    // Get existing incharge for a department
    public HODIncharge getByDept(String dept) {
        return repo.findByDepartment(dept).orElse(null);
    }

    public List<HODIncharge> getHistory() {
        return repo.findAllByOrderByAssignedAtDesc();
    }
}
