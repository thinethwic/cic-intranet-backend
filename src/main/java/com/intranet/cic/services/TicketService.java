package com.intranet.cic.services;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.types.TicketCategory;
import com.intranet.cic.entities.types.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {


    // ─── Employee ───────────────────────────────────────────────
    TicketDTO createTicket(TicketDTO ticketDTO);
    Page<TicketDTO> getMyTickets(Pageable pageable);          // current user's tickets only
    TicketDTO getTicketById(Long id);

    // ─── Admin / Handler ────────────────────────────────────────
    Page<TicketDTO> getAllTickets(Pageable pageable);          // all tickets, no filter
    Page<TicketDTO> getTicketsByStatus(Pageable pageable, TicketStatus status);
    Page<TicketDTO> getTicketsByCategory(Pageable pageable, TicketCategory category);
    TicketDTO assignTicket(Long ticketId, Long userId);
    TicketDTO updateTicketStatus(Long ticketId, TicketStatus newStatus);

    // ─── Comments ───────────────────────────────────────────────
    TicketCommentDTO addComment(Long ticketId, TicketCommentDTO commentDTO);
    Page<TicketCommentDTO> getCommentsByTicket(Pageable pageable, Long ticketId);

    // ─── Shared ─────────────────────────────────────────────────
    TicketDTO updateTicket(Long ticketId, TicketDTO ticketDTO);
    void deleteTicket(Long ticketId);
}
