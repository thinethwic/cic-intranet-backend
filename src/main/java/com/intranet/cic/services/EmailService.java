package com.intranet.cic.services;

public interface EmailService {
    void sendNewTicketNotification(String ticketNumber, String title,
                                   String description, String priority,
                                   String segment, String department, String submittedBy, String email) ;
}
