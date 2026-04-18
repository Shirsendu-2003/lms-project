package com.example.lms.repository;

import com.example.lms.model.ActivityLog;

import com.example.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findAllByOrderByTimeDesc();



}
