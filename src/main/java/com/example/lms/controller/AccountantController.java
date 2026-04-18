package com.example.lms.controller;

import com.example.lms.dto.AccountantApplicationResponse;
import com.example.lms.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accountant")
@CrossOrigin
public class AccountantController {

    private final ApplicationService applicationService;

    public AccountantController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ✅ Accountant will see only PIC-approved WITHOUT PAY applications
    @GetMapping("/approved")
    public ResponseEntity<List<AccountantApplicationResponse>> getApprovedForAccountant() {
        return ResponseEntity.ok(applicationService.findApprovedForAccountant());
    }
}
