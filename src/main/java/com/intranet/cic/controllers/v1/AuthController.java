package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.LoginDTO;
import com.intranet.cic.dtos.LoginResponseDTO;
import com.intranet.cic.entities.AuditLog;
import com.intranet.cic.services.AuditLogService;
import com.intranet.cic.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
public class AuthController extends AbstractController {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpRequest;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return sendOkResponse(authService.login(loginDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null) {
            auditLogService.record(
                    authentication.getName(),
                    null,
                    AuditLog.EventType.LOGOUT,
                    AuditLog.EventStatus.SUCCESS,
                    getClientIp(),
                    httpRequest.getHeader("User-Agent"),
                    null
            );
        }
        return ResponseEntity.ok().build();
    }

    private String getClientIp() {
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = httpRequest.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return httpRequest.getRemoteAddr();
    }
}
