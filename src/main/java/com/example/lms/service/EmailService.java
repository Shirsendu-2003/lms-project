package com.example.lms.service;

import com.example.lms.model.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.util.List;
import com.example.lms.dto.AdjustmentItem;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public void sendPlainEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your Password Reset OTP");
        msg.setText(
                "Your OTP to reset the password is: " + otp +
                        "\n\nThis code will expire in 10 minutes." +
                        "\nIf you did not request this, please ignore this email."
        );
        mailSender.send(msg);
    }

    public void sendLeaveAdjustmentMail(String to, Application app, String action) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(
                    "Leave Application Updated by OS – " + app.getApplicationId()
            );

            String adjustmentHtml = buildAdjustmentHtml(app);

            String html = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color:#333;">
                    
                        <p>Dear <strong>%s</strong>,</p>
                    
                        <p>
                            Your leave application has been reviewed by the
                            <strong>Office Superintendent (OS)</strong>.
                        </p>
                    
                        <table style="border-collapse:collapse;width:100%%;margin-top:10px;">
                            <tr style="background:#f2f2f2;">
                                <th style="border:1px solid #ccc;padding:8px;">Field</th>
                                <th style="border:1px solid #ccc;padding:8px;">Details</th>
                            </tr>
                    
                            <tr>
                                <td style="border:1px solid #ccc;padding:8px;">Application ID</td>
                                <td style="border:1px solid #ccc;padding:8px;">%s</td>
                            </tr>
                    
                            <tr>
                                <td style="border:1px solid #ccc;padding:8px;">Original Leave</td>
                                <td style="border:1px solid #ccc;padding:8px;">
                                    %s – <strong>%d day(s)</strong>
                                </td>
                            </tr>
                    
                            %s
                    
                            <tr style="background:#eef6ff;">
                                <td style="border:1px solid #ccc;padding:8px;">OS Action</td>
                                <td style="border:1px solid #ccc;padding:8px;">
                                    <strong>%s</strong>
                                </td>
                            </tr>
                    
                            <tr>
                                <td style="border:1px solid #ccc;padding:8px;">Current Status</td>
                                <td style="border:1px solid #ccc;padding:8px;">
                                    <strong>%s</strong>
                                </td>
                            </tr>
                        </table>
                    
                        <p style="margin-top:12px;">
                            The application has now been forwarded to
                            <strong>Principal In Charge (PIC)</strong> for final decision.
                        </p>
                    
                        <p>Regards,<br><strong>LMS – Office Superintendent</strong></p>
                    
                    </body>
                    </html>
                    """.formatted(
                    app.getStaffName(),
                    app.getApplicationId(),
                    app.getType(),
                    app.getDays(),
                    adjustmentHtml,
                    action,
                    app.getOverallStatus()
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OS adjustment mail", e);
        }
    }

    /* ======================
       ADJUSTMENT HTML BUILDER
    ====================== */

    private String buildAdjustmentHtml(Application app) {

        // 🔁 MULTIPLE ADJUSTMENT (SL → CL + PL)
        if (app.getAdjustedBreakup() != null && !app.getAdjustedBreakup().isBlank()) {
            try {
                List<AdjustmentItem> items =
                        objectMapper.readValue(
                                app.getAdjustedBreakup(),
                                new TypeReference<List<AdjustmentItem>>() {
                                }
                        );

                StringBuilder sb = new StringBuilder();
                for (AdjustmentItem i : items) {
                    sb.append("• ")
                            .append(i.getType())
                            .append(" – <strong>")
                            .append(i.getDays())
                            .append(" day(s)</strong><br/>");
                }

                return """
                        <tr style="background:#fff7e6;">
                            <td style="border:1px solid #ccc;padding:8px;">Adjusted Leave</td>
                            <td style="border:1px solid #ccc;padding:8px;">
                                %s
                            </td>
                        </tr>
                        """.formatted(sb.toString());

            } catch (Exception e) {
                throw new RuntimeException("Failed to parse adjusted breakup", e);
            }
        }

        // 🔹 SINGLE ADJUSTMENT (SL → PL)
        if (app.getAdjustedType() != null) {
            return """
                    <tr style="background:#fff7e6;">
                        <td style="border:1px solid #ccc;padding:8px;">Adjusted Leave</td>
                        <td style="border:1px solid #ccc;padding:8px;">
                            %s – <strong>%d day(s)</strong>
                        </td>
                    </tr>
                    """.formatted(
                    app.getAdjustedType(),
                    app.getAdjustedDays()
            );
        }

        return "";
    }



    /* =========================
       PIC EMAIL (FINAL)
    ========================== */

    public void sendPICFinalMail(String to, Application app, String decision) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(
                    "Leave Application " + decision + " – " + app.getApplicationId()
            );

            String bgColor;
            String textColor;
            String decisionText;

            switch (decision.toUpperCase()) {
                case "APPROVED" -> {
                    bgColor = "#e6fffa";
                    textColor = "green";
                    decisionText = "APPROVED";
                }
                case "REJECTED" -> {
                    bgColor = "#ffe6e6";
                    textColor = "red";
                    decisionText = "REJECTED";
                }
                case "WITHOUT_PAY" -> {
                    bgColor = "#fff7e6";
                    textColor = "#b45309"; // amber
                    decisionText = "APPROVED WITHOUT PAY";
                }
                default -> throw new RuntimeException("Invalid PIC decision: " + decision);
            }

            String html = """
        <html>
        <body style="font-family: Arial, sans-serif; color:#333;">
            <p>Dear <strong>%s</strong>,</p>

            <p>
                Your leave application has been
                <strong style="color:%s;">%s</strong>
                by the <strong>Principal In Charge (PIC)</strong>.
            </p>

            <table style="border-collapse:collapse;width:100%%;background:%s;">
                <tr style="background:#f2f2f2;">
                    <th style="border:1px solid #ccc;padding:8px;">Field</th>
                    <th style="border:1px solid #ccc;padding:8px;">Details</th>
                </tr>

                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Application ID</td>
                    <td style="border:1px solid #ccc;padding:8px;">%s</td>
                </tr>

                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Final Leave Type</td>
                    <td style="border:1px solid #ccc;padding:8px;">%s</td>
                </tr>

                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Final Days</td>
                    <td style="border:1px solid #ccc;padding:8px;">%d</td>
                </tr>

                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Final Status</td>
                    <td style="border:1px solid #ccc;padding:8px;">
                        <strong>%s</strong>
                    </td>
                </tr>
            </table>

            <p style="margin-top:10px;">
                This is the <strong>final decision</strong>.
            </p>

            <p>Regards,<br><strong>LMS – PIC Office</strong></p>
        </body>
        </html>
        """.formatted(
                    app.getStaffName(),
                    textColor,
                    decisionText,
                    bgColor,
                    app.getApplicationId(),
                    app.getAdjustedType() != null ? app.getAdjustedType() : app.getType(),
                    app.getAdjustedDays() != null ? app.getAdjustedDays() : app.getDays(),
                    app.getOverallStatus()
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send PIC final mail", e);
        }
    }

    public void sendHodRejectionMail(String to, Application app, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Leave Application Rejected by HOD – " + app.getApplicationId());

            String html = """
        <html>
        <body style="font-family:Arial,sans-serif;color:#333;">
            <p>Dear <strong>%s</strong>,</p>

            <p>
                Your leave application has been
                <strong style="color:red;">REJECTED</strong>
                by the <strong>Head of Department</strong>.
            </p>

            <table style="border-collapse:collapse;width:100%%;">
                <tr style="background:#f2f2f2;">
                    <th style="border:1px solid #ccc;padding:8px;">Field</th>
                    <th style="border:1px solid #ccc;padding:8px;">Details</th>
                </tr>
                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Application ID</td>
                    <td style="border:1px solid #ccc;padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Leave Type</td>
                    <td style="border:1px solid #ccc;padding:8px;">%s</td>
                </tr>
                <tr>
                    <td style="border:1px solid #ccc;padding:8px;">Period</td>
                    <td style="border:1px solid #ccc;padding:8px;">%s → %s</td>
                </tr>
                <tr style="background:#ffe6e6;">
                    <td style="border:1px solid #ccc;padding:8px;">Reason</td>
                    <td style="border:1px solid #ccc;padding:8px;"><strong>%s</strong></td>
                </tr>
            </table>

            <p>Please contact your department for clarification.</p>

            <p>Regards,<br><strong>LMS – HOD Office</strong></p>
        </body>
        </html>
        """.formatted(
                    app.getStaffName(),
                    app.getApplicationId(),
                    app.getType(),
                    app.getFromDate(),
                    app.getToDate(),
                    reason != null && !reason.isBlank()
                            ? reason
                            : "No remarks provided"
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HOD rejection mail", e);
        }
    }



}
