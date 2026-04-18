package com.example.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class OSBulkAdjustmentRequest {
    private List<AdjustmentItem> adjustments;
    private String comment;
}



