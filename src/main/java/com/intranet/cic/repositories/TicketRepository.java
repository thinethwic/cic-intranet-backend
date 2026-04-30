package com.intranet.cic.repositories;

import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.TicketCategory;
import com.intranet.cic.entities.types.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(t.ticketNumber, 10) AS int)), 0) " +
            "FROM Ticket t WHERE t.ticketNumber LIKE CONCAT('TKT-', :year, '-%')")
    long findMaxSequenceForYear(@Param("year") int year);


    Page<Ticket> findBySubmittedBy(User submittedBy, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    Page<Ticket> findByCategory(TicketCategory category, Pageable pageable);
}
