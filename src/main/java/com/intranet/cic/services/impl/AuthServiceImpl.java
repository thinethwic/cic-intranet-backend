package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.LoginDTO;
import com.intranet.cic.dtos.LoginResponseDTO;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        try {
            User user = userRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new IntranetException(
                            "Invalid email or password", HttpStatus.UNAUTHORIZED));

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                log.warn("Failed login attempt for email: {}", loginDTO.getEmail());
                throw new IntranetException("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }

            String token = generateToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return new LoginResponseDTO(
                    token,
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole().name()
            );
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", loginDTO.getEmail(), e);
            throw new IntranetException("Login failed. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateToken(User user) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.builder()
                    .subject(String.valueOf(user.getId()))
                    .claim("email", user.getEmail())
                    .claim("name", user.getName())
                    .claim("username", user.getUsername())
                    .claim("role", user.getRole().name())
                    .claim("location", user.getSegment())
                    .claim("department", user.getDepartment())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", user.getEmail(), e);
            throw new IntranetException("Failed to generate authentication token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}