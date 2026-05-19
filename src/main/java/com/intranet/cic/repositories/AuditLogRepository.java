package com.intranet.cic.repositories;

import com.intranet.cic.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Filtered, paginated query — all parameters are optional.
     * Passing null for any filter means "no filter applied".
     */
    @Query(value = """
    SELECT * FROM audit_logs a
    WHERE (CAST(:username AS text) IS NULL OR lower(cast(a.username AS text)) LIKE lower(concat('%', :username, '%')))
      AND (CAST(:eventType AS text) IS NULL OR a.event_type = :eventType)
      AND (CAST(:status AS text)    IS NULL OR a.status     = :status)
      AND (CAST(:from AS timestamp) IS NULL OR a.created_at >= :from)
      AND (CAST(:to   AS timestamp) IS NULL OR a.created_at <= :to)
    """,
            countQuery = """
    SELECT count(*) FROM audit_logs a
    WHERE (CAST(:username AS text) IS NULL OR lower(cast(a.username AS text)) LIKE lower(concat('%', :username, '%')))
      AND (CAST(:eventType AS text) IS NULL OR a.event_type = :eventType)
      AND (CAST(:status AS text)    IS NULL OR a.status     = :status)
      AND (CAST(:from AS timestamp) IS NULL OR a.created_at >= :from)
      AND (CAST(:to   AS timestamp) IS NULL OR a.created_at <= :to)
    """,
            nativeQuery = true)
    Page<AuditLog> findFiltered(
            @Param("username")  String username,
            @Param("eventType") String eventType,
            @Param("status")    String status,
            @Param("from")      LocalDateTime from,
            @Param("to")        LocalDateTime to,
            Pageable pageable
    );

    // ── Stats counts ───────────────────────────────────────────────────────

    long countByEventType(AuditLog.EventType eventType);

    long countByEventTypeAndStatus(AuditLog.EventType eventType, AuditLog.EventStatus status);

    @Query("SELECT COUNT(DISTINCT a.username) FROM AuditLog a")
    long countDistinctUsers();
}

