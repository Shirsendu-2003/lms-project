package com.example.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String otp;

    @NotBlank @Size(min = 6)
    private String newPassword;
    // getters/setters
}
