package com.example.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "staff_id", unique = true, nullable = false)
    private String staffId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String mobileNo;
    private String dateOfJoining; // stored as plain text

    private boolean active = true;
    private boolean blocked = false;

    private LocalDateTime lastUpdate = LocalDateTime.now();

    private boolean incharge;

    @Column(nullable = false)
    private int clBalance = 15;

    @Column(nullable = false)
    private int plBalance = 28;

    @Column(nullable = false)
    private int elBalance = 28;

    @Column(nullable = false)
    private int mlBalance = 10;
}
