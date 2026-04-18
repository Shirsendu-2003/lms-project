package com.example.lms.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    private String staffId;
    private String name;
    private String email;
    private String password;
    private String department;
    private String mobileNo;
    private String role;
    private String dateOfJoining;

}
