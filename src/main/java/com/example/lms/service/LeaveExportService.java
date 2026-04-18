package com.example.lms.service;

import com.example.lms.model.Application;
import com.example.lms.repository.ApplicationRepository;
import com.itextpdf.text.Font;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// PDF imports
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
@RequiredArgsConstructor
public class LeaveExportService {

    private final ApplicationRepository applicationRepository;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

    // ---------------------------- STAFF WISE ----------------------------

    private List<Application> getForStaff(String staffId, int month, int year) {
        return applicationRepository.findMonthlyLeaveByStaff(staffId, month, year);
    }

    // ✅ PDF (Staff)
    public byte[] generateStaffPDF(String staffId, int month, int year) {

        List<Application> list = getForStaff(staffId, month, year);

        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            if (!list.isEmpty()) {
                Application first = list.get(0);

                Paragraph header = new Paragraph(
                        "Monthly Leave Statement\n\n" +
                                "Name: " + first.getStaffName() + "\n" +
                                "Staff ID: " + staffId + "\n" +
                                "Department: " + first.getDepartment() + "\n" +
                                "Month: " + month + "  Year: " + year + "\n" +
                                "Generated On: " + LocalDateTime.now().format(formatter) +
                                "\n\n",
                        new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
                );
                document.add(header);
            }

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            String[] headers = {"Type", "From", "To", "Days", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (Application a : list) {
                table.addCell(a.getType());
                table.addCell(a.getFromDate().toString());
                table.addCell(a.getToDate().toString());
                table.addCell(String.valueOf(a.getDays()));
                table.addCell(a.getOverallStatus());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // ✅ Excel (Staff)
    public byte[] generateStaffExcel(String staffId, int month, int year) {

        List<Application> list = getForStaff(staffId, month, year);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Leave Statement");

            // 🔥 ADD GENERATED ON
            Row gen = sheet.createRow(0);
            gen.createCell(0).setCellValue("Generated On: " + LocalDateTime.now().format(formatter));

            Row header = sheet.createRow(1);
            String[] cols = {"Type", "From", "To", "Days", "Status"};

            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            int rowIdx = 2;
            for (Application a : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getType());
                row.createCell(1).setCellValue(a.getFromDate().toString());
                row.createCell(2).setCellValue(a.getToDate().toString());
                row.createCell(3).setCellValue(a.getDays());
                row.createCell(4).setCellValue(a.getOverallStatus());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ---------------------------- ALL STAFF ----------------------------

    private List<Application> getAllForDepartment(String dept, int month, int year) {
        return applicationRepository.findMonthlyLeaveByDepartment(dept, month, year);
    }

    // ✅ PDF (All Staff)
    public byte[] generateAllStaffPDF(String dept, int month, int year) {

        List<Application> list = getAllForDepartment(dept, month, year);

        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            Paragraph header = new Paragraph(
                    "Monthly Leave Statement (All Staff of Department)\n\n" +
                            "Department: " + dept + "\n" +
                            "Month: " + month + "  Year: " + year + "\n" +
                            "Generated On: " + LocalDateTime.now().format(formatter) +
                            "\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
            );
            document.add(header);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);

            String[] headers = {"Name", "Staff ID", "Type", "From", "To", "Days", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (Application a : list) {
                table.addCell(a.getStaffName());
                table.addCell(a.getStaffId());
                table.addCell(a.getType());
                table.addCell(a.getFromDate().toString());
                table.addCell(a.getToDate().toString());
                table.addCell(String.valueOf(a.getDays()));
                table.addCell(a.getOverallStatus());
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate All Staff PDF", e);
        }
    }

    // ✅ Excel (All Staff)
    public byte[] generateAllStaffExcel(String dept, int month, int year) {

        List<Application> list = getAllForDepartment(dept, month, year);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("All Staff Leave");

            // 🔥 ADD GENERATED ON
            Row gen = sheet.createRow(0);
            gen.createCell(0).setCellValue("Generated On: " + LocalDateTime.now().format(formatter));

            Row header = sheet.createRow(1);
            String[] cols = {"Name", "Staff ID", "Type", "From", "To", "Days", "Status"};

            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            int rowIdx = 2;
            for (Application a : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getStaffName());
                row.createCell(1).setCellValue(a.getStaffId());
                row.createCell(2).setCellValue(a.getType());
                row.createCell(3).setCellValue(a.getFromDate().toString());
                row.createCell(4).setCellValue(a.getToDate().toString());
                row.createCell(5).setCellValue(a.getDays());
                row.createCell(6).setCellValue(a.getOverallStatus());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate All Staff Excel", e);
        }
    }
}
