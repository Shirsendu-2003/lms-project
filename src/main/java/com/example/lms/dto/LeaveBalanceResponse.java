package com.example.lms.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveBalanceResponse {
    private int clBalance;
    private int plBalance;
    private int mlBalance;
    private int elBalance;
}
