package com.example.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private String applicationId;
    private String staffId;
    private String staffName;
    private String department;

    private String type;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int days;

    private String dateOfJoining;

    private String adjustedType;
    private Integer adjustedDays;
    private boolean adjustmentApplied;

    private boolean withoutPayRequest;

    // NEW: filename or downloadable URL (relative)
    private String medicalCertificateFilename;   // raw filename stored in DB
    private String medicalCertificateUrl;




    private String hodStatus;
    private String osStatus;
    private String picStatus;
    private String acStatus;
    private String overallStatus;
    private String reason;

    private LocalDate appliedOn;
    private String adjustedBreakup;
}
