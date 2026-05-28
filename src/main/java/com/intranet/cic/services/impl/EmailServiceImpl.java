package com.intranet.cic.services.impl;

import com.intranet.cic.services.EmailService;
import com.intranet.cic.services.FileStorageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final FileStorageService fileStorageService;

    private final JavaMailSender mailSender;

    @Value("${app.superuser.email}")
    private String superUserEmail;

    @Value("${app.ticket.url}")
    private String ticketUrl;

    @Async
    public void sendNewTicketNotification(String ticketNumber, String title,
                                          String description, String priority,
                                          String segment, String department,
                                          LocalDateTime createdAt, String submittedBy,
                                          String email, List<String> attachmentUrls) {
        List<Path> tempFiles = new ArrayList<>();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String[] recipients = Arrays.stream(superUserEmail.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            helper.setTo(recipients);
            helper.setSubject("New Support Ticket: " + ticketNumber);
            helper.setText(buildEmailBody(ticketNumber, title, description,
                    priority, segment, department, createdAt, submittedBy, email), true);

            // ── Download each GCS file to temp and attach ──
            if (attachmentUrls != null) {
                for (String url : attachmentUrls) {
                    try {
                        Path tempFile = fileStorageService.resolveFilePath(url);
                        tempFiles.add(tempFile);
                        String filename = url.substring(url.lastIndexOf('/') + 1);
                        helper.addAttachment(filename, tempFile.toFile());
                    } catch (Exception e) {
                        log.warn("Could not attach file from URL {}: {}", url, e.getMessage());
                    }
                }
            }

            mailSender.send(message);
            log.info("Ticket notification sent for {} to {} recipients with {} attachment(s)",
                    ticketNumber, recipients.length,
                    attachmentUrls != null ? attachmentUrls.size() : 0);

        } catch (MessagingException e) {
            log.error("Failed to send ticket notification for {}: {}", ticketNumber, e.getMessage());
        } finally {
            // ── Clean up temp files ──
            for (Path temp : tempFiles) {
                try { Files.deleteIfExists(temp); } catch (Exception ignored) {}
            }
        }
    }
// Custom Email Section
//    @Async
//    public void sendNewTicketNotification(String ticketNumber, String title,
//                                          String description, String priority,
//                                          String segment, String department,
//                                          LocalDateTime createdAt, String submittedBy,
//                                          String email, List<String> recipientEmails) {
//        try {
//            if (recipientEmails == null || recipientEmails.isEmpty()) {
//                log.warn("No recipients found for ticket {}, skipping notification", ticketNumber);
//                return;
//            }
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            String[] recipients = recipientEmails.stream()
//                    .filter(e -> e != null && !e.isBlank())
//                    .toArray(String[]::new);
//
//            if (recipients.length == 0) {
//                log.warn("All recipient emails were blank for ticket {}", ticketNumber);
//                return;
//            }
//
//            helper.setTo(recipients);
//            helper.setSubject("New Support Ticket: " + ticketNumber);
//            helper.setText(buildEmailBody(ticketNumber, title, description,
//                    priority, segment, department, createdAt, submittedBy, email), true);
//
//            mailSender.send(message);
//            log.info("Ticket notification sent for {} to: {}", ticketNumber, recipientEmails);
//        } catch (MessagingException e) {
//            log.error("Failed to send ticket notification for {}: {}", ticketNumber, e.getMessage());
//        }
//    }

    private String tableRow(String label, String value) {
        return "<tr>"
                + "<td style=\"padding:10px 14px;border:1px solid #d1d5db;width:220px;vertical-align:top;"
                + "font-weight:600;color:#374151;background:#f9fafb;white-space:nowrap;\">" + label + "</td>"
                + "<td style=\"padding:10px 14px;border:1px solid #d1d5db;color:#1e293b;vertical-align:top;"
                + "word-break:break-word;\">" + value + "</td>"
                + "</tr>";
    }

    private String buildEmailBody(String ticketNumber, String title,
                                  String description, String priority,
                                  String segment, String department,
                                  LocalDateTime createdAt, String submittedBy, String email) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        String formattedDate = createdAt.format(formatter);

        // preserve line breaks in description
        String formattedDescription = description
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");

        return "<div style=\"font-family:Arial,sans-serif;max-width:700px;margin:0;color:#333;\">"

                // ── Intro ──
                + "<p style=\"font-size:14px;color:#333;margin:0 0 20px 0;line-height:1.6;\">"
                + "A support case has been opened for you. We will review the details provided and get back to you as soon as possible."
                + "</p>"

                // ── Table ──
                + "<table style=\"width:100%;border-collapse:collapse;font-size:14px;margin-bottom:24px;"
                + "border:1px solid #d1d5db;\">"

                + tableRow("Ticket ID",          ticketNumber)
                + tableRow("Created Date",       formattedDate)
                + tableRow("Request Subject",    title)
                + tableRow("Requester",          submittedBy)
                + tableRow("Email",              email)
                + tableRow("Site / Segment",     segment)
                + tableRow("Department",         department)
                + tableRow("Priority",           priority)

                // ── Description row (longer content) ──
                + "<tr>"
                + "<td style=\"padding:10px 14px;border:1px solid #d1d5db;width:220px;vertical-align:top;"
                + "font-weight:600;color:#374151;background:#f9fafb;white-space:nowrap;\">Request Description</td>"
                + "<td style=\"padding:10px 14px;border:1px solid #d1d5db;color:#1e293b;vertical-align:top;"
                + "word-break:break-word;line-height:1.7;min-width:0;\">"
                + formattedDescription
                + "</td>"
                + "</tr>"

                + "</table>"

                // ── View Ticket Button ──
                + "<div style=\"margin-top:24px;text-align:center;\">"
                + "<a href=\"" + ticketUrl + "\" "
                + "style=\"display:inline-block;background:#1e3a5f;color:white;text-decoration:none;"
                + "padding:12px 32px;border-radius:8px;font-weight:bold;font-size:14px;\">"
                + "View Ticket &rarr;"
                + "</a>"
                + "</div>"

                + "<p style=\"margin-top:16px;font-size:12px;color:#94a3b8;text-align:left;\">"
                + "Or copy this link: <a href=\"" + ticketUrl + "\" style=\"color:#3b82f6;\">" + ticketUrl + "</a>"
                + "</p>"

                + "</div>";
    }
}
