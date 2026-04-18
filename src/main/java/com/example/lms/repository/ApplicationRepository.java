package com.example.lms.repository;

import com.example.lms.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // --- basic single-field finders (you already had these) ---
    List<Application> findByStaffId(String staffId);

    List<Application> findByDepartmentAndHodStatus(String department, String status);
    List<Application> findByHodStatus(String status);

    List<Application> findByOsStatus(String status);

    List<Application> findByPicStatus(String status);

    List<Application> findByAcStatus(String status);

    List<Application> findByTypeAndPicStatusAndAcStatus(
            String type,
            String picStatus,
            String acStatus
    );

    List<Application> findByPicStatusAndPaymentStatus(String picStatus, String paymentStatus);

    List<Application> findByOverallStatus(String overallStatus);

    // --- multi-field finders used by getPendingForRole() in Service ---
    // OS should see only those where HOD approved but OS not processed yet (PENDING)
    List<Application> findByHodStatusAndOsStatus(String hodStatus, String osStatus);
    List<Application> findByHodStatusAndOsStatusOrderByFromDateAsc(String hodStatus, String osStatus);

    // PIC should see only those where OS approved and PIC pending
    List<Application> findByOsStatusAndPicStatus(String osStatus, String picStatus);
    List<Application> findByOsStatusAndPicStatusOrderByFromDateAsc(String osStatus, String picStatus);

    // AC should see only those where PIC approved and AC pending
    List<Application> findByPicStatusAndAcStatus(String picStatus, String acStatus);
    List<Application> findByPicStatusAndAcStatusOrderByFromDateAsc(String picStatus, String acStatus);

    // --- helpful history / processed queries (existing) ---
    @Query("SELECT a FROM Application a WHERE a.osStatus IS NOT NULL")
    List<Application> findAllProcessedByOS();

    @Query("SELECT a FROM Application a WHERE a.hodStatus IS NOT NULL")
    List<Application> getHodHistory();

    Application findTopByStaffIdOrderByAppliedOnDesc(String staffId);
    Application findByApplicationId(String applicationId);

    @Query("""
    SELECT a FROM Application a
    WHERE a.staffId = :staffId
      AND MONTH(a.fromDate) = :month
      AND YEAR(a.fromDate) = :year
      AND a.overallStatus = 'APPROVED'
""")
    List<Application> findMonthlyLeaveByStaff(
            @Param("staffId") String staffId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT a FROM Application a " +
            "WHERE a.department = :dept " +
            "AND MONTH(a.fromDate) = :month " +
            "AND YEAR(a.fromDate) = :year")
    List<Application> findMonthlyLeaveByDepartment(
            @Param("dept") String dept,
            @Param("month") int month,
            @Param("year") int year);

    List<Application> findByPicStatusAndOverallStatus(String picStatus, String overallStatus);









}
