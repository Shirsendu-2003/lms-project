package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.model.Application;
import com.example.lms.model.User;
import com.example.lms.repository.ApplicationRepository;
import com.example.lms.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;


    // ✅ Application ID generator
    private String generateApplicationId(Long id) {
        return String.format("APP%03d", id); // Example: APP001, APP045, APP350
    }

    // Absolute safe directory
    private final Path medicalUploadDir =
            Paths.get("C:/lms/uploads/medical");;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository, EmailService emailService, ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    // ✅ APPLY LEAVE
    // new entrypoint: handles file optionally
    public ApplicationResponse applyWithCertificate(ApplicationRequest req, MultipartFile medicalCertificate) {
        // create directories if needed
        try {
            if (!Files.exists(medicalUploadDir)) {
                Files.createDirectories(medicalUploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        // call existing internal apply logic (build Application)
        User user = userRepository.findByStaffId(req.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found: " + req.getStaffId()));

        // ================= DOCUMENT VALIDATION =================
        LocalDate today = LocalDate.now();

        // 🔒 SL → document ALWAYS mandatory
        if ("SL".equalsIgnoreCase(req.getType())) {
            if (medicalCertificate == null || medicalCertificate.isEmpty()) {
                throw new RuntimeException(
                        "Supporting document is mandatory for Special Leave (SL)"
                );
            }
        }

        // 🔒 ML → document mandatory ONLY if leave already started
        if ("ML".equalsIgnoreCase(req.getType())) {
            if (req.getFromDate().isBefore(today)) {
                if (medicalCertificate == null || medicalCertificate.isEmpty()) {
                    throw new RuntimeException(
                            "Medical certificate required since leave is already taken"
                    );
                }
            }
        }

        Application app = new Application();
        app.setUser(user);
        app.setStaffId(req.getStaffId());
        app.setStaffName(req.getStaffName());
        app.setDepartment(user.getDepartment().name());
        app.setType(req.getType());
        app.setDateOfJoining(user.getDateOfJoining());
        app.setFromDate(req.getFromDate());
        app.setToDate(req.getToDate());

        long days = ChronoUnit.DAYS.between(req.getFromDate(), req.getToDate()) + 1;
        app.setDays((int) days);

        app.setReason(req.getReason());
        app.setAppliedOn(LocalDate.now());

        // workflow defaults
        app.setHodStatus("PENDING");
        app.setOsStatus("PENDING");
        app.setPicStatus("PENDING");
        app.setAcStatus("PENDING");
        app.setOverallStatus("PENDING");

        // deduction flags
        app.setLeaveDeducted(false);
        app.setAdjustedType(null);
        app.setAdjustedDays(null);
        app.setAdjustmentApplied(false);

        // If ML type and file provided, save file
        // ================= SAVE FILE (SL or ML) =================
        if (medicalCertificate != null && !medicalCertificate.isEmpty()) {

            String original =
                    StringUtils.cleanPath(medicalCertificate.getOriginalFilename());

            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);

            String fileName =
                    req.getStaffId() + "_" + UUID.randomUUID() + ext;

            Path target =
                    medicalUploadDir.resolve(fileName).normalize();

            try {
                medicalCertificate.transferTo(target.toFile());
                app.setMedicalCertificateFilename(fileName);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to store medical certificate", e
                );
            }
        }


        // ✅ ✅ FIRST SAVE → to generate DB auto-increment ID
        Application saved = applicationRepository.save(app);

        // ✅ Create formatted applicationId using DB ID
        saved.setApplicationId(generateApplicationId(saved.getId()));

        // ✅ Save again with final applicationId
        saved = applicationRepository.save(saved);

        return mapToResponseWithFileUrl(saved);
    }

    // helper map that includes file URL
    private ApplicationResponse mapToResponseWithFileUrl(Application a) {
        ApplicationResponse resp = mapToResponse(a);
        String filename = a.getMedicalCertificateFilename();
        if (filename != null && !filename.isBlank()) {
            // return a relative URL for frontend to hit file-serving endpoint
            resp.setMedicalCertificateFilename(filename);
            resp.setMedicalCertificateUrl("/api/files/medical/" + filename);
        }
        return resp;
    }

    // ✅ DETAILS (by applicationId)
    public ApplicationResponse getDetails(String applicationId) {
        Application app = applicationRepository.findByApplicationId(applicationId);
        if (app == null) throw new RuntimeException("Application not found");
        return mapToResponse(app);
    }


    // ✅ STAFF HISTORY
    public List<ApplicationResponse> history(String staffId) {
        return applicationRepository.findByStaffId(staffId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Application getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    // ✅ HOD PENDING LIST (DEPARTMENT LEVEL)
    public List<ApplicationResponse> getPendingForHodStaff(String staffId) {
        User hod = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("HOD not found"));

        String dept = hod.getDepartment().name();

        return applicationRepository.findByDepartmentAndHodStatus(dept, "PENDING")
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ✅ GENERIC PENDING
    public List<ApplicationResponse> getPendingForRole(String role) {
        role = role.toUpperCase();

        List<Application> list = switch (role) {
            case "HOD" -> applicationRepository.findByHodStatus("PENDING");
            case "OS" -> applicationRepository.findByHodStatusAndOsStatus("APPROVED", "PENDING");
            case "PIC" -> applicationRepository.findByOsStatusAndPicStatus("FORWARD_PIC", "PENDING");
            case "ACC" -> applicationRepository
                    .findByPicStatusAndOverallStatus("WITHOUT_PAY", "PENDING");

            default -> throw new RuntimeException("Invalid role: " + role);
        };

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }


    // ✅ UPDATE STATUS ENGINE
    // ---------------- STATUS UPDATE ----------------
    @Transactional
    public ApplicationResponse updateStatus(Long id, String role, String action, String comment) {

        Application app = getById(id);
        role = role.toUpperCase();

        switch (role) {
            case "HOD" -> {
                if (!List.of("APPROVED", "REJECTED").contains(action.toUpperCase()))
                    throw new RuntimeException("HOD can only APPROVE or REJECT");

                app.setHodStatus(action.toUpperCase());
                app.setHodComment(comment);

                if ("REJECTED".equalsIgnoreCase(action)) {
                    app.setOverallStatus("REJECTED");

                    app.setOsStatus("NOT_APPLICABLE");
                    app.setPicStatus("NOT_APPLICABLE");

                    // ✅ SAVE FIRST
                    Application saved = applicationRepository.save(app);

                    // 📧 SEND MAIL TO STAFF
                    emailService.sendHodRejectionMail(
                            saved.getUser().getEmail(),
                            saved,
                            comment
                    );

                    return mapToResponse(saved);
                }
            }


            case "OS" -> {

                if (!"APPROVED".equalsIgnoreCase(app.getHodStatus()))
                    throw new RuntimeException("HOD must approve first.");



                if (!"FORWARD_PIC".equalsIgnoreCase(action))
                    throw new RuntimeException("OS can only FORWARD_PIC");

                app.setOsStatus("FORWARD_PIC");
                app.setOsComment(comment);

                Application saved = applicationRepository.save(app);

                emailService.sendLeaveAdjustmentMail(
                        saved.getUser().getEmail(),
                        saved,
                        "FORWARDED TO PIC"
                );

                return mapToResponse(saved);
            }


            case "PIC" -> {
                if (!List.of("APPROVED", "REJECTED", "WITHOUT_PAY").contains(action.toUpperCase()))
                    throw new RuntimeException("PIC can only APPROVE, REJECT or WITHOUT_PAY");

                app.setPicStatus(action.toUpperCase());
                app.setPicComment(comment);

                if ("REJECTED".equalsIgnoreCase(action)) {
                    app.setOverallStatus("REJECTED");

                    Application saved = applicationRepository.save(app);

                    emailService.sendPICFinalMail(
                            saved.getUser().getEmail(),
                            saved,
                            "REJECTED"
                    );

                    return mapToResponse(saved);
                }

                if ("APPROVED".equalsIgnoreCase(action)) {
                    app.setOverallStatus("APPROVED");
                    subtractLeaveBalance(app);

                    Application saved = applicationRepository.save(app);

                    emailService.sendPICFinalMail(
                            saved.getUser().getEmail(),
                            saved,
                            "APPROVED"
                    );


                    return mapToResponse(saved);
                }

                if ("WITHOUT_PAY".equalsIgnoreCase(action)) {
                    app.setOverallStatus("WITHOUT_PAY");
                    app.setAcStatus("PENDING");

                    Application saved = applicationRepository.save(app);

                    emailService.sendPICFinalMail(
                            saved.getUser().getEmail(),
                            saved,
                            "WITHOUT_PAY"
                    );

                    return mapToResponse(saved);
                }
            }



            // ✅ new
            case "ACC" -> {
                throw new RuntimeException("Accountant is view-only. No action permitted.");
            }



            default -> throw new RuntimeException("Invalid role: " + role);
        }

        return mapToResponse(applicationRepository.save(app));
    }




    // ✅ APPLY ADJUSTMENT (Universal)
    public void adjustLeave(AdjustmentRequest req) {
        Application app = applicationRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setAdjustedType(req.getAdjustedType().toUpperCase());
        app.setAdjustedDays(req.getAdjustedDays());
        app.setAdjustmentApplied(false);

        applicationRepository.save(app);
    }



    // ✅ OS ADJUSTMENT
    @Transactional
    public ApplicationResponse applyOSAdjustment(Long id, String newType, Integer days) {

        Application app = getById(id);

        // ✅ RULE 1: Only SL applications can be adjusted by OS
        if (!"SL".equalsIgnoreCase(app.getType())) {
            throw new RuntimeException("OS adjustment allowed only for Special Leave (SL)");
        }

        if (!"PENDING".equalsIgnoreCase(app.getOsStatus()))
            throw new RuntimeException("OS can adjust only before OS approval.");

        if (days == null || days <= 0)
            throw new RuntimeException("Adjusted days must be > 0");

        app.setAdjustedType(newType.toUpperCase());
        app.setAdjustedDays(days);
        app.setAdjustmentApplied(false);
        app.setOsStatus("ADJUSTED");

        // ✅ SAVE FIRST
        Application saved = applicationRepository.save(app);

        // 📧 SEND MAIL AFTER SAVE
        emailService.sendLeaveAdjustmentMail(
                saved.getUser().getEmail(),
                saved,
                "LEAVE ADJUSTED BY OS"
        );

        return mapToResponse(saved);
    }

    @Transactional
    public ApplicationResponse applyOSBulkAdjustment(Long id, OSBulkAdjustmentRequest req) {

        Application app = getById(id);

        if (!"SL".equalsIgnoreCase(app.getType()))
            throw new RuntimeException("OS adjustment allowed only for SL");

        if (!"PENDING".equalsIgnoreCase(app.getOsStatus()))
            throw new RuntimeException("OS already adjusted");

        int totalAdjusted = req.getAdjustments()
                .stream()
                .mapToInt(AdjustmentItem::getDays)
                .sum();

        if (totalAdjusted > app.getDays())
            throw new RuntimeException("Adjusted days exceed SL days");

        try {
            app.setAdjustedBreakup(
                    objectMapper.writeValueAsString(req.getAdjustments())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize adjustment breakup", e);
        }

        app.setOsComment(req.getComment());
        app.setOsStatus("FORWARD_PIC");

        Application saved = applicationRepository.save(app);

        emailService.sendLeaveAdjustmentMail(
                saved.getUser().getEmail(),
                saved,
                "LEAVE ADJUSTED BY OS"
        );

        return mapToResponse(saved);
    }








    // ✅ DEDUCT LEAVE BALANCE
    private void subtractLeaveBalance(Application app) {

        if (app.isLeaveDeducted()) return;

        User user = app.getUser();

        try {

            // ✅ CASE 1: OS SPLIT ADJUSTMENT (SL → PL + ML etc.)
            if (app.getAdjustedBreakup() != null && !app.getAdjustedBreakup().isBlank()) {

                List<AdjustmentItem> items =
                        objectMapper.readValue(
                                app.getAdjustedBreakup(),
                                new com.fasterxml.jackson.core.type.TypeReference<>() {}
                        );

                for (AdjustmentItem item : items) {
                    String type = item.getType().toUpperCase();
                    int days = item.getDays();

                    switch (type) {
                        case "CL" -> user.setClBalance(user.getClBalance() - days);
                        case "PL" -> user.setPlBalance(user.getPlBalance() - days);
                        case "EL" -> user.setElBalance(user.getElBalance() - days);
                        case "ML" -> user.setMlBalance(user.getMlBalance() - days);
                        case "WITHOUT_PAY" -> {}
                        default -> throw new RuntimeException("Invalid leave type: " + type);
                    }
                }

                app.setLeaveDeducted(true);
                app.setAdjustmentApplied(true);

                userRepository.save(user);
                applicationRepository.save(app);
                return;
            }

            // ✅ CASE 2: SINGLE ADJUSTMENT OR NORMAL LEAVE
            int days = app.getAdjustedDays() != null
                    ? app.getAdjustedDays()
                    : app.getDays();

            String type = app.getAdjustedType() != null
                    ? app.getAdjustedType()
                    : app.getType();

            switch (type.toUpperCase()) {
                case "CL" -> user.setClBalance(user.getClBalance() - days);
                case "PL" -> user.setPlBalance(user.getPlBalance() - days);
                case "EL" -> user.setElBalance(user.getElBalance() - days);
                case "ML" -> user.setMlBalance(user.getMlBalance() - days);
                case "WITHOUT_PAY" -> {}
                default -> throw new RuntimeException("Invalid leave type: " + type);
            }

            app.setLeaveDeducted(true);
            app.setAdjustmentApplied(true);

            userRepository.save(user);
            applicationRepository.save(app);

        } catch (Exception e) {
            throw new RuntimeException("Leave deduction failed", e);
        }
    }


    private int deduct(IntSupplier getter, IntConsumer setter, int days) {
        int balance = getter.getAsInt();
        int used = Math.min(balance, days);
        setter.accept(balance - used);
        return days - used;
    }



    // ✅ ADMIN VIEW ALL
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ✅ ACCOUNTANT VIEW
    public List<AccountantApplicationResponse> findApprovedForAccountant() {

        List<Application> list =
                applicationRepository.findByOverallStatus("WITHOUT_PAY");

        return list.stream().map(a ->
                AccountantApplicationResponse.builder()
                        .applicationId(a.getApplicationId())
                        .staffId(a.getStaffId())
                        .staffName(a.getStaffName())
                        .department(a.getDepartment())
                        .type(a.getType())
                        .fromDate(a.getFromDate())
                        .toDate(a.getToDate())
                        .days(a.getDays())
                        .picStatus(a.getPicStatus())
                        .acStatus("VIEW_ONLY")
                        .appliedOn(a.getAppliedOn())
                        .build()
        ).collect(Collectors.toList());
    }


    // ✅ OS HISTORY
    public List<Application> getOSHistory() {
        return applicationRepository.findAllProcessedByOS();  // OK
    }


    // ✅ HOD HISTORY
    public List<ApplicationResponse> getHodHistory() {
        return applicationRepository.getHodHistory()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ RESPONSE MAPPER
    public ApplicationResponse mapToResponse(Application a) {
        return ApplicationResponse.builder()
                .id(a.getId())
                .applicationId(a.getApplicationId())
                .staffId(a.getStaffId())
                .staffName(a.getStaffName())
                .department(a.getDepartment())
                .type(a.getType())
                .fromDate(a.getFromDate())
                .toDate(a.getToDate())
                .days(a.getDays())
                .dateOfJoining(a.getDateOfJoining())
                .hodStatus(a.getHodStatus())
                .osStatus(a.getOsStatus())
                .picStatus(a.getPicStatus())
                .acStatus(a.getAcStatus())
                .overallStatus(a.getOverallStatus())
                .appliedOn(a.getAppliedOn())
                .reason(a.getReason())
                .adjustedBreakup(a.getAdjustedBreakup())
                .adjustedType(a.getAdjustedType())
                .adjustedDays(a.getAdjustedDays())
                .adjustmentApplied(a.isAdjustmentApplied())
                .medicalCertificateFilename(a.getMedicalCertificateFilename())
                .medicalCertificateUrl(a.getMedicalCertificateFilename() != null ? "/api/files/medical/" + a.getMedicalCertificateFilename() : null)
                .build();
    }


    // ✅ LAST LEAVE STATUS CHECK
    public LastApplyResponse checkLastApplyStatus(String staffId) {

        // Fetch user
        User user = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Fetch last application (most recent)
        Application last = applicationRepository.findTopByStaffIdOrderByAppliedOnDesc(staffId);

        // Case 1: No previous leave
        if (last == null) {
            return LastApplyResponse.builder()
                    .allowed(true)
                    .message("First time leave — allowed")
                    .lastAppliedDate(null)
                    .daysLeft(0)
                    .build();
        }

        LocalDate lastTo = last.getToDate();
        LocalDate today = LocalDate.now();

        long diffDays = ChronoUnit.DAYS.between(lastTo, today); // Days since last leave ended

        // Case 2: Allowed if gap >= 7 days
        if (diffDays >= 7) {
            return LastApplyResponse.builder()
                    .allowed(true)
                    .message("Allowed. Last applied " + diffDays + " day(s) ago.")
                    .lastAppliedDate(last.getAppliedOn()) // ✅ changed from lastTo
                    .daysLeft(0)
                    .build();
        }

        // Case 3: Block if gap < 7 days
        long daysLeft = 7 - diffDays; // remaining days until next allowed leave
        return LastApplyResponse.builder()
                .allowed(false)
                .message("Leave blocked. Next leave allowed in " + daysLeft + " day(s).")
                .lastAppliedDate(last.getAppliedOn()) // ✅ changed from lastTo
                .daysLeft(daysLeft)
                .build();
    }

    public List<ApplicationResponse> getPendingForAccountant() {
        return applicationRepository.findByOverallStatus("WITHOUT_PAY")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }




}
