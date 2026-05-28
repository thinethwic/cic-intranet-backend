package com.intranet.cic.services;

import java.time.LocalDateTime;
import java.util.List;
//import java.util.List;

public interface EmailService {
    void sendNewTicketNotification(String ticketNumber, String title,
                                   String description, String priority,
                                   String segment, String department, LocalDateTime createdAt,
                                   String submittedBy, String email, List<String> attachmentUrls);
}
