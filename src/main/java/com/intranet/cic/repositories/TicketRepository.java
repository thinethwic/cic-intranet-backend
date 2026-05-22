package com.intranet.cic.repositories;

import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.entities.types.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket,Long> {

    long countByTicketNumberStartingWith(String prefix);
    boolean existsByTicketNumber(String ticketNumber);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND ((:dept IS NULL AND t.department IS NULL) OR t.department = :dept)")
    long findMaxSequenceForYearAndDepartment(@Param("year") int year, @Param("dept") String dept);


    Page<Ticket> findBySubmittedBy(User submittedBy, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findBySegment(Segment segment, Pageable pageable);

    /**
     * IT Admin routing — cross-segment.
     *
     * Returns every ticket whose category matches 'IT' (case-insensitive),
     * regardless of which segment raised it.  Used when the logged-in admin
     * has department = "IT"; the IT function is centralised so IT admins
     * are not scoped to a single segment.
     */

    @Query("SELECT t FROM Ticket t WHERE UPPER(t.category) = UPPER(:category)")
    Page<Ticket> findByCategory(@Param("category") String category, Pageable pageable);

    /**
     * Non-IT Admin routing — segment AND department scoped.
     * Returns tickets where both segment and department match the admin's
     * own segment and department.  Used for HR, Finance, Facilities, etc.
     * admins who are confined to their own business unit.
     */

    @Query("SELECT t FROM Ticket t WHERE t.segment = :segment " +
            "AND LOWER(t.department) = LOWER(:department)")
    Page<Ticket> findBySegmentAndDepartmentIgnoreCase(@Param("segment") Segment segment,
                                                      @Param("department") String department,
                                                      Pageable pageable);

}
