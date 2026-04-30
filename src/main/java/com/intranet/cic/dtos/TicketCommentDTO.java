package com.intranet.cic.dtos;

import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.User;
import lombok.Data;

@Data
public class TicketCommentDTO {

    private Long id;

    private Ticket ticket;

    private String message;

    private User commentedBy;

    private Boolean isInternal = false;
}
