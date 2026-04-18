package com.example.lms.dto;
import com.example.lms.model.User;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
}
