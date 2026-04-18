package com.example.lms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private String name;
    private String email;
    private String password;     // optional – default password created if empty
    private String department;   // HOD, CSE, IT, OFFICE_STAFF etc.
    private String mobileNo;
    private String role;         // HOD, PIC, OS, STAFF etc.


    private String dateOfJoining; // ✅ NEW (string from frontend)
}
