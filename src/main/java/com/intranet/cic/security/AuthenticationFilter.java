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
    private final UserRepository userRepository; // ✅ inject

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && tokenValidator.validateToken(token)) {
            String username   = tokenValidator.extractUsername(token);
            String email      = tokenValidator.extractEmail(token);
            String role       = tokenValidator.extractRole(token);
            String location   = tokenValidator.extractLocation(token);
            String department = tokenValidator.extractDepartment(token);

            // ✅ Block inactive users — reject even with a valid token
            String lookupName = username != null ? username : email;
            boolean isActive = userRepository.findActiveByUsername(lookupName)
                    .orElse(false);

            if (!isActive) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Account is disabled\"}");
                return; // ← stop filter chain, don't authenticate
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

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}