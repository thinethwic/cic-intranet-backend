package com.intranet.cic.repositories;

import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment,Long> {
    Page<TicketComment> findByTicketOrderByCreatedAtAsc(Ticket ticket, Pageable pageable);
    List<TicketComment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
