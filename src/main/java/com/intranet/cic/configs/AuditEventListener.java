package com.intranet.cic.configs;

import com.intranet.cic.entities.AuditLog;
import com.intranet.cic.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Listens to Spring Security authentication events and writes audit records.
 *
 * Spring Security fires these automatically — no changes needed to your
 * login/logout endpoints as long as you use Spring Security's standard flow.
 *
 * For logout, wire this into your LogoutSuccessHandler (see logoutSuccessHandler below).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogService auditLogService;

    // ── Successful login ───────────────────────────────────────────────────

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = extractUsername(event.getAuthentication().getPrincipal());
        String name     = extractName(event.getAuthentication().getPrincipal());

        auditLogService.record(
                username, name,
                AuditLog.EventType.LOGIN, AuditLog.EventStatus.SUCCESS,
                getClientIp(), getUserAgent(),
                null
        );

        log.info("LOGIN SUCCESS: {}", username);
    }

    // ── Failed login ───────────────────────────────────────────────────────

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = String.valueOf(event.getAuthentication().getPrincipal());
        String reason   = event.getException().getMessage();

        auditLogService.record(
                username, null,
                AuditLog.EventType.LOGIN_FAILED, AuditLog.EventStatus.FAILURE,
                getClientIp(), getUserAgent(),
                reason
        );

        log.warn("LOGIN FAILED: {} — {}", username, reason);
    }

    // ── Logout — call this from your LogoutSuccessHandler ─────────────────

    /**
     * Call this method from your custom LogoutSuccessHandler:
     *
     *   auditEventListener.recordLogout(authentication, request);
     *
     * Or register a LogoutSuccessHandler bean and inject AuditLogService there directly.
     */
    public void recordLogout(String username, String name) {
        auditLogService.record(
                username, name,
                AuditLog.EventType.LOGOUT, AuditLog.EventStatus.SUCCESS,
                getClientIp(), getUserAgent(),
                null
        );

        log.info("LOGOUT: {}", username);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String extractUsername(Object principal) {
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return String.valueOf(principal);
    }

    private String extractName(Object principal) {
        // If your UserDetails implementation has a getName() method, cast here.
        // Otherwise return null and the audit log will show username only.
        return null;
    }

    private String getClientIp() {
        try {
            HttpServletRequest req = currentRequest();
            if (req == null) return null;
            // Check common proxy headers first
            String ip = req.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
            ip = req.getHeader("X-Real-IP");
            if (ip != null && !ip.isBlank()) return ip;
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserAgent() {
        try {
            HttpServletRequest req = currentRequest();
            return req != null ? req.getHeader("User-Agent") : null;
        } catch (Exception e) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
