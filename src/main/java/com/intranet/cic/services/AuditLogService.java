package com.intranet.cic.services;

import com.intranet.cic.entities.AuditLog;
import org.springframework.data.domain.Page;

import java.io.PrintWriter;
import java.util.Map;

public interface AuditLogService {
    void record(
            String username,
            String name,
            AuditLog.EventType eventType,
            AuditLog.EventStatus status,
            String ipAddress,
            String userAgent,
            String failureReason
    );
    Page<AuditLog> findFiltered(
            String username,
            String eventTypeStr,
            String statusStr,
            String fromStr,
            String toStr,
            int page,
            int size
    );

    Map<String, Long> getStats();

    void exportCsv(
            String username,
            String eventTypeStr,
            String statusStr,
            String fromStr,
            String toStr,
            PrintWriter writer
    );
}
