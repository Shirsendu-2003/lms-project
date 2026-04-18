package com.example.lms.controller;

import com.example.lms.service.LeaveExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@CrossOrigin  // <-- Add if frontend needs access
public class LeaveExportController {

    private final LeaveExportService leaveExportService;

    // ======================= STAFF EXPORTS =======================

    // PDF Export for one staff
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportStaffPDF(
            @RequestParam String staffId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        byte[] pdf = leaveExportService.generateStaffPDF(staffId, month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=staff_leave_statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Excel Export for one staff
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportStaffExcel(
            @RequestParam String staffId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        byte[] excel = leaveExportService.generateStaffExcel(staffId, month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=staff_leave_statement.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(excel);
    }

    // ======================= DEPARTMENT EXPORTS =======================

    // PDF Export for ALL staff in department
    @GetMapping("/export-all/pdf")
    public ResponseEntity<byte[]> exportAllStaffPDF(
            @RequestParam String dept,
            @RequestParam int month,
            @RequestParam int year
    ) {
        byte[] pdf = leaveExportService.generateAllStaffPDF(dept, month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=all_staff_leave_statement_" + dept + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Excel Export for ALL staff in department
    @GetMapping("/export-all/excel")
    public ResponseEntity<byte[]> exportAllStaffExcel(
            @RequestParam String dept,
            @RequestParam int month,
            @RequestParam int year
    ) {
        byte[] excel = leaveExportService.generateAllStaffExcel(dept, month, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=all_staff_leave_statement_" + dept + ".xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(excel);
    }
}
