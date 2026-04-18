package com.example.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LastApplyResponse {
    private boolean allowed;
    private String message;
    private LocalDate lastAppliedDate;
    private long daysLeft;
}
