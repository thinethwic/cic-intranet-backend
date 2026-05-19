package com.intranet.cic.controllers.v1;

import com.intranet.cic.entities.AuditLog;
import com.intranet.cic.services.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")   // only SUPER_ADMIN can access
public class AuditLogController {

    private final AuditLogService service;

    /**
     * GET /api/v1/audit-logs
     * All parameters are optional.
     *
     * @param username   partial match on username
     * @param eventType  LOGIN | LOGOUT | LOGIN_FAILED
     * @param status     SUCCESS | FAILURE
     * @param from       date string yyyy-MM-dd  (inclusive)
     * @param to         date string yyyy-MM-dd  (inclusive)
     * @param page       0-based page index (default 0)
     * @param size       page size (default 10)
     */
    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.findFiltered(username, eventType, status, from, to, page, size);
    }

    /**
     * GET /api/v1/audit-logs/stats
     * Returns total counts for the summary cards.
     */
    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return service.getStats();
    }

    /**
     * GET /api/v1/audit-logs/export
     * Streams a CSV file with the filtered results.
     */
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=audit-logs.csv"
        );
        service.exportCsv(username, eventType, status, from, to, response.getWriter());
    }
}

