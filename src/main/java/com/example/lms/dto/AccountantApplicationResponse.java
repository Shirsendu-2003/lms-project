package com.example.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class AccountantApplicationResponse {
    private String applicationId;
    private String staffId;
    private String staffName;
    private String department;

    private String type;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int days;

    private String picStatus;  // ✅ REQUIRED
    private String acStatus;   // ✅ REQUIRED

    private String overallStatus;
    private LocalDate appliedOn;
    private String reason;
}
