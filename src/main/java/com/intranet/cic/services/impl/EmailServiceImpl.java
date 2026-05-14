package com.intranet.cic.services.impl;

import com.intranet.cic.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.superuser.email}")
    private String superUserEmail;

    @Value("${app.ticket.url}")
    private String ticketUrl;

    @Async
    public void sendNewTicketNotification(String ticketNumber, String title,
                                          String description, String priority,
                                          String segment, String department, String submittedBy,String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // ✅ Split comma-separated emails into array
            String[] recipients = Arrays.stream(superUserEmail.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            helper.setTo(recipients);
            helper.setSubject("New Support Ticket: " + ticketNumber);
            helper.setText(buildEmailBody(ticketNumber, title, description,
                    priority, segment, department,submittedBy,email), true);

            mailSender.send(message);
            log.info("Ticket notification sent for {} to {} recipients", ticketNumber, recipients.length);
        } catch (MessagingException e) {
            log.error("Failed to send ticket notification for {}: {}", ticketNumber, e.getMessage());
        }
    }

    private String buildEmailBody(String ticketNumber, String title,
                                  String description, String priority,
                                  String segment, String department, String submittedBy, String email) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;\">"
                + "<div style=\"background:#1e3a5f;padding:24px;border-radius:12px 12px 0 0;\">"
                + "<h2 style=\"color:white;margin:0;\">New Support Ticket</h2>"
                + "<p style=\"color:#93c5fd;margin:4px 0 0;\">A new ticket has been submitted</p>"
                + "</div>"
                + "<div style=\"border:1px solid #e2e8f0;border-top:none;padding:24px;border-radius:0 0 12px 12px;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                + "<tr><td style=\"padding:8px 0;color:#64748b;width:130px;\">Ticket #</td>"
                + "    <td style=\"padding:8px 0;font-weight:bold;\">" + ticketNumber + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Title</td>"
                + "    <td style=\"padding:8px 0;\">" + title + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Priority</td>"
                + "    <td style=\"padding:8px 0;\">" + priority + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Location</td>"
                + "    <td style=\"padding:8px 0;\">" + segment + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Department</td>"
                + "    <td style=\"padding:8px 0;\">" + department + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Submitted By</td>"
                + "    <td style=\"padding:8px 0;\">" + submittedBy + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;color:#64748b;\">Submitted By Email</td>"
                + "    <td style=\"padding:8px 0;\">" + email + "</td></tr>"
                + "</table>"
                + "<div style=\"margin-top:16px;background:#f8fafc;padding:16px;border-radius:8px;\">"
                + "<p style=\"color:#64748b;margin:0 0 8px;font-size:12px;text-transform:uppercase;\">Description</p>"
                + "<p style=\"margin:0;color:#1e293b;\">" + description + "</p>"
                + "</div>"
                // ✅ View Ticket button
                + "<div style=\"margin-top:24px;text-align:center;\">"
                + "<a href=\"" + ticketUrl + "\" "
                + "style=\"display:inline-block;background:#1e3a5f;color:white;text-decoration:none;"
                + "padding:12px 32px;border-radius:8px;font-weight:bold;font-size:14px;\">"
                + "View Ticket &rarr;"
                + "</a>"
                + "</div>"
                + "<p style=\"margin-top:16px;font-size:12px;color:#94a3b8;text-align:center;\">"
                + "Or copy this link: <a href=\"" + ticketUrl + "\" style=\"color:#3b82f6;\">" + ticketUrl + "</a>"
                + "</p>"
                + "</div>"
                + "</div>";
    }
}
