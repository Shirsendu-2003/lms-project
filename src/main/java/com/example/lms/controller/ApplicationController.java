package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.model.Application;
import com.example.lms.repository.ApplicationRepository;
import com.example.lms.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    public ApplicationController(ApplicationService applicationService, ApplicationRepository applicationRepository) {
        this.applicationService = applicationService;
        this.applicationRepository = applicationRepository;
    }



    // ✅ APPLY LEAVE
    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApplicationResponse apply(
            @RequestParam String staffId,
            @RequestParam String staffName,
            @RequestParam String department,
            @RequestParam String type,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String reason,
            @RequestParam String doj,
            @RequestParam(value = "medicalCertificate", required = false)
            MultipartFile medicalCertificate
    ) {

        ApplicationRequest req = ApplicationRequest.builder()
                .staffId(staffId)
                .staffName(staffName)
                .department(department)
                .type(type)
                .fromDate(fromDate)
                .toDate(toDate)
                .reason(reason)
                .dateOfJoining(doj)     // ✅ FIXED
                .build();

        return applicationService.applyWithCertificate(req, medicalCertificate);
    }

    // ✅ Get application details
    @GetMapping("/details/{applicationId}")
    public ApplicationResponse details(@PathVariable String applicationId) {
        return applicationService.getDetails(applicationId);
    }



    // ✅ STAFF HISTORY
    @GetMapping("/history/{staffId}")
    public List<ApplicationResponse> history(@PathVariable String staffId) {
        return applicationService.history(staffId);
    }

    // ✅ PENDING FOR SPECIFIC ROLE (HOD / OS / PIC / AC)
    @GetMapping("/pending/role/{role}")
    public List<ApplicationResponse> getPendingByRole(@PathVariable String role) {
        return applicationService.getPendingForRole(role);
    }

    // ✅ PENDING FOR HOD (DEPARTMENT-WISE)
    @GetMapping("/pending/hod/{staffId}")
    public List<ApplicationResponse> getPendingForHod(@PathVariable String staffId) {
        return applicationService.getPendingForHodStaff(staffId);
    }

    @PreAuthorize("hasAnyRole('HOD','OS','PIC','ACC')")
    @PostMapping("/{applicationId}/action")
    public ApplicationResponse updateStatus(
            @PathVariable String applicationId,
            @RequestParam String role,
            @RequestParam String action,
            @RequestParam(required = false) String comment
    ) {
        Application app = applicationRepository.findByApplicationId(applicationId);
        if (app == null) throw new RuntimeException("Application not found");
        return applicationService.updateStatus(app.getId(), role, action, comment);
    }

    // ✅ Universal Adjustment (matches React)
    @PostMapping("/adjust")
    public ResponseEntity<String> adjust(@RequestBody AdjustmentRequest request) {
        applicationService.adjustLeave(request);
        return ResponseEntity.ok("Adjustment saved");
    }




    @PostMapping("/os-adjust/{applicationId}")
    public ApplicationResponse osManualAdjust(
            @PathVariable String applicationId,
            @RequestParam String newType,
            @RequestParam Integer days
    ) {
        Application app = applicationRepository.findByApplicationId(applicationId);
        if (app == null) throw new RuntimeException("Application not found");
        return applicationService.applyOSAdjustment(app.getId(), newType, days);
    }




    // ✅ ALL APPLICATIONS
    @GetMapping("/all")
    public List<ApplicationResponse> all() {
        return applicationService.getAllApplications();
    }

    // ✅ OS HISTORY
    @GetMapping("/history/os")
    public List<ApplicationResponse> osHistory() {
        return applicationService.getOSHistory()
                .stream()
                .map(applicationService::mapToResponse)
                .toList();
    }


    // ✅ HOD HISTORY
    @GetMapping("/history/hod")
    public List<ApplicationResponse> hodHistory() {
        return applicationService.getHodHistory();
    }

    @GetMapping("/last/{staffId}")
    public ResponseEntity<LastApplyResponse> lastApply(@PathVariable String staffId) {
        return ResponseEntity.ok(applicationService.checkLastApplyStatus(staffId));
    }

    @PostMapping("/os-adjust-bulk/{applicationId}")
    public ApplicationResponse osAdjustBulk(
            @PathVariable String applicationId,
            @RequestBody OSBulkAdjustmentRequest req
    ) {
        Application app = applicationRepository.findByApplicationId(applicationId);
        if (app == null) throw new RuntimeException("Application not found");

        return applicationService.applyOSBulkAdjustment(app.getId(), req);
    }

    @GetMapping("/pending/accountant")
    @PreAuthorize("hasAnyRole('ACC','ACCOUNTANT')")
    public List<ApplicationResponse> getPendingForAccountant() {
        return applicationService.getPendingForAccountant();
    }





}
