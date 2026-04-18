package com.example.lms.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ActionRequest {
    public String role;
    public String action; // APPROVE / REJECT / FORWARD
    public String comment;
}
