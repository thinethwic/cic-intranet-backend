package com.intranet.cic.security;

import com.intranet.cic.repositories.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final TokenValidator tokenValidator;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null && tokenValidator.validateToken(token)) {
            String username   = tokenValidator.extractUsername(token);
            String email      = tokenValidator.extractEmail(token);
            String role       = tokenValidator.extractRole(token);
            String location   = tokenValidator.extractLocation(token);
            String department = tokenValidator.extractDepartment(token);

            String lookupName = username != null ? username : email;
            boolean isActive = userRepository.findActiveByUsername(lookupName)
                    .orElse(false);

            if (!isActive) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized or Token has expired\", \"status\": 401}");
                return;
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }

            var principal = new org.springframework.security.core.userdetails.User(
                    lookupName,
                    "",
                    authorities
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            authentication.setDetails(java.util.Map.of(
                    "location",   location   != null ? location   : "",
                    "department", department != null ? department : "",
                    "email",      email      != null ? email      : ""
            ));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        if (method.equals("POST") && path.startsWith("/api/v1/users")) return true;
        if (method.equals("POST") && path.startsWith("/api/public")) return true;

        // Alerts — /all requires auth, everything else is public
        if (method.equals("GET") && path.startsWith("/api/v1/alerts")
                && !path.equals("/api/v1/alerts/all")) return true;

        if (method.equals("GET")  && path.startsWith("/api/v1/members")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/news")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/events")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/images")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/videos")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/documents") && !path.endsWith("/logs")) return true;
        if (method.equals("GET")  && path.startsWith("/api/v1/users")) return true;
        if (method.equals("GET")  && path.startsWith("/api/public")) return true;
        if (path.startsWith("/uploads/")) return true;
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}