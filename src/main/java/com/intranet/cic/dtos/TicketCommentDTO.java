package com.intranet.cic.dtos;

import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketCommentDTO {
    private Long id;
    private String message;
    private CommentedByDTO commentedBy;  // nested DTO instead of raw User
    private Boolean isInternal = false;
    private LocalDateTime createdAt;

    @Data
    public static class CommentedByDTO {
        private Long id;
        private String name;
        private String role;  // ← "ADMIN", "AUTHORIZED", "USER", etc.
    }
}
