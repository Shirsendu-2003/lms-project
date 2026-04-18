package com.example.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String actor;     // Who performed the action (could include role)
    private String action;    // e.g., "set as in-charge", "removed from in-charge"
    private String target;    // Target staff name
    private LocalDateTime time;
}
