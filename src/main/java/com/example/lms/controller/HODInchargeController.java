package com.example.lms.controller;

import com.example.lms.dto.HODInchargeRequest;
import com.example.lms.model.HODIncharge;
import com.example.lms.service.HODInchargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HODInchargeController {

    private final HODInchargeService service;

    //  Save or Update Incharge
    @PostMapping("/incharge")
    public ResponseEntity<?> saveIncharge(@RequestBody HODInchargeRequest req) {
        HODIncharge saved = service.save(req);
        return ResponseEntity.ok(saved);
    }

    //  Get Incharge by Department
    @GetMapping("/incharge")
    public ResponseEntity<?> getIncharge(@RequestParam String dept) {
        HODIncharge inc = service.getByDept(dept);
        return ResponseEntity.ok(inc);
    }

    @GetMapping("/incharge/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(service.getHistory());
    }
}
