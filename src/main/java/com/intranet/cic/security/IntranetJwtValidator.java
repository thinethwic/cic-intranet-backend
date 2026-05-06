package com.intranet.cic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

@Slf4j
public class IntranetJwtValidator implements TokenValidator{

    private final String secretKey;

    public IntranetJwtValidator(String secretKey) {
        this.secretKey = secretKey;
    }


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    @Override
    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public String extractUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    @Override
    public String extractName(String token) {
        return getClaims(token).get("name", String.class);
    }

    @Override
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    @Override
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    @Override
    public String extractLocation(String token) {
        return getClaims(token).get("location", String.class);
    }

    @Override
    public String extractDepartment(String token) {
        return getClaims(token).get("department", String.class);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Failed to validate JWT token: {}",e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
