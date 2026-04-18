package com.example.lms.repository;

import com.example.lms.model.HODIncharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HODInchargeRepository extends JpaRepository<HODIncharge, Long> {

    Optional<HODIncharge> findByDepartment(String dept);

    List<HODIncharge> findAllByOrderByAssignedAtDesc();

    Optional<HODIncharge> findByDepartmentAndActiveTrue(String dept);

    List<HODIncharge> findAllByDepartmentOrderByAssignedAtDesc(String dept);

}
