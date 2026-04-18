package com.example.lms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hod_incharge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HODIncharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String staffId;  // selected incharge staff

    private String assignedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    @Column(nullable = false)
    private boolean active;   // 🔥 REQUIRED for history & current tracking
}
