package com.intranet.cic.repositories;


import com.intranet.cic.entities.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    // Fetch categories matching segment+department, or global ones (null)
    @Query("""
        SELECT c FROM TicketCategory c
        WHERE c.active = true
        AND (c.segment IS NULL OR c.segment = :segment)
        AND (c.department IS NULL OR c.department = :department)
        ORDER BY c.name ASC
    """)
    List<TicketCategory> findApplicable(
            @Param("segment") String segment,
            @Param("department") String department
    );

    List<TicketCategory> findAllByOrderByNameAsc(); // for admin
}
