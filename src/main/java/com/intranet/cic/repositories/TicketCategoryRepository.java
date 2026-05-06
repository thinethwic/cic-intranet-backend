package com.intranet.cic.repositories;


import com.intranet.cic.entities.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    // Used when segment + department both provided
    @Query("""
        SELECT c FROM TicketCategory c
        WHERE c.active = true
        AND (c.segment IS NULL OR c.segment = :segment)
        AND (c.department IS NULL OR c.department = :department)
        ORDER BY c.name ASC
    """)
    List<TicketCategory> findBySegmentAndDepartment(
            @Param("segment") String segment,
            @Param("department") String department
    );

    // Used when only segment is provided (no department)
    @Query("""
        SELECT c FROM TicketCategory c
        WHERE c.active = true
        AND (c.segment IS NULL OR c.segment = :segment)
        ORDER BY c.name ASC
    """)
    List<TicketCategory> findBySegmentOnly(
            @Param("segment") String segment
    );

    List<TicketCategory> findAllByOrderByNameAsc();
}
