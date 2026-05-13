package com.intranet.cic.repositories;

import com.intranet.cic.entities.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    // AlertRepository.java
    @Query("SELECT a FROM Alert a WHERE a.date IS NULL OR a.date <= :today ORDER BY a.createdAt DESC")
    Page<Alert> findAllPublished(@Param("today") LocalDate today, Pageable pageable);
}
