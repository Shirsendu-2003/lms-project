package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {
    private String staffId;
    private String staffName;
    private String department;

    private String type;
    private String dateOfJoining;
    private LocalDate fromDate;
    private LocalDate toDate;

    private String reason;
}
