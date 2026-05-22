package com.intranet.cic.schedulers;

import com.intranet.cic.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogScheduler {

    private final AuditLogService auditLogService;

    /**
     * Runs every hour.
     * Deletes all audit log records older than 24 hours.
     *
     * Cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 * * * *")  // ← every hour
    public void purgeOldAuditLogs() {
        log.info("Starting scheduled audit log purge...");
        auditLogService.scheduledPurge();
    }
}