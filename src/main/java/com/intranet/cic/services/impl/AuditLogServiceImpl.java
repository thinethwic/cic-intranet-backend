package com.intranet.cic.services.impl;

import com.intranet.cic.entities.AuditLog;
import com.intranet.cic.repositories.AuditLogRepository;
import com.intranet.cic.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repo;

    // ── Write ──────────────────────────────────────────────────────────────

    /**
     * Call this from your Spring Security event listeners to persist an event.
     */
    public void record(
            String username,
            String name,
            AuditLog.EventType eventType,
            AuditLog.EventStatus status,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .name(name)
                .eventType(eventType)
                .status(status)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .build();
        repo.save(log);
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public Page<AuditLog> findFiltered(
            String username,
            String eventTypeStr,
            String statusStr,
            String fromStr,
            String toStr,
            int page,
            int size
    ) {
        AuditLog.EventType eventType = parseEnum(AuditLog.EventType.class,   eventTypeStr);
        AuditLog.EventStatus status    = parseEnum(AuditLog.EventStatus.class, statusStr);
        LocalDateTime from    = fromStr != null && !fromStr.isBlank()
                ? LocalDate.parse(fromStr).atStartOfDay() : null;
        LocalDateTime to      = toStr   != null && !toStr.isBlank()
                ? LocalDate.parse(toStr).atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));

        return repo.findFiltered(
                username,
                eventType != null ? eventType.name() : null,
                status    != null ? status.name()    : null,
                from,
                to,
                pageable
        );
    }

    // ── Stats ──────────────────────────────────────────────────────────────

    public Map<String, Long> getStats() {
        return Map.of(
                "totalLogins",   repo.countByEventTypeAndStatus(AuditLog.EventType.LOGIN,  AuditLog.EventStatus.SUCCESS),
                "totalLogouts",  repo.countByEventType(AuditLog.EventType.LOGOUT),
                "totalFailed",   repo.countByEventType(AuditLog.EventType.LOGIN_FAILED),
                "uniqueUsers",   repo.countDistinctUsers()
        );
    }

    // ── CSV Export ─────────────────────────────────────────────────────────

    public void exportCsv(
            String username,
            String eventTypeStr,
            String statusStr,
            String fromStr,
            String toStr,
            PrintWriter writer
    ) {
        // Fetch up to 50,000 rows for export (adjust as needed)
        Page<AuditLog> page = findFiltered(
                username, eventTypeStr, statusStr, fromStr, toStr, 0, 50_000
        );

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        writer.println("ID,Username,Name,Event,Status,IP Address,Failure Reason,Timestamp");
        for (AuditLog log : page.getContent()) {
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    csv(log.getUsername()),
                    csv(log.getName()),
                    log.getEventType(),
                    log.getStatus(),
                    csv(log.getIpAddress()),
                    csv(log.getFailureReason()),
                    log.getCreatedAt() != null ? log.getCreatedAt().format(fmt) : ""
            );
        }
        writer.flush();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private <E extends Enum<E>> E parseEnum(Class<E> clazz, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(clazz, value.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Escape a CSV field — wraps in quotes if it contains commas or quotes */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }
}
