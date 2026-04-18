package com.example.lms.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuthRequest {
    private String email;
    private String staffId;
    private String password;
    private String role; // optional
}
