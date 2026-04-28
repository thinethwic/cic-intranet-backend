package com.intranet.cic.security;



public interface TokenValidator {
    boolean validateToken(String token);
    String extractUserId(String token);
    String extractUsername (String token);
    String extractName(String token);
    String extractEmail(String token);
    String extractRole(String token);
}
