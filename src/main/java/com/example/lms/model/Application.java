package com.example.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String applicationId;   // e.g., APP001


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String staffId;
    private String staffName;
    private String department;

    private String type; // CL, PL, ML, EL, WITHOUT_PAY, SL etc.

    private String dateOfJoining; // plain text

    private LocalDate fromDate;
    private LocalDate toDate;
    private int days;

    private String hodStatus;
    private String hodComment;

    private String osStatus;
    private String osComment;

    @Column(name = "pic_status")
    private String picStatus;
    private String picComment;

    private String acStatus;
    private String acComment;

    @Column(name = "payment_status")
    private String paymentStatus;

    private String overallStatus;

    private LocalDate appliedOn;
    private String reason;

    // Prevent double deduction
    private boolean leaveDeducted = false;

    // ✅ Add these 3 fields
    private String adjustedType;
    private Integer adjustedDays;

    @Column(nullable = false)
    private boolean adjustmentApplied = false;

    // WITHOUT_PAY request flag — keep original `type` intact but mark request
    private boolean withoutPayRequest = false;

    // --- new field to store uploaded medical certificate filename (if ML)
    private String medicalCertificateFilename;

    @Column(columnDefinition = "TEXT") // or JSON for MySQL 8 / Postgres
    private String adjustedBreakup;




}
