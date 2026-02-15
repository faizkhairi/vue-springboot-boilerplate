package com.app.boilerplate.auth;

/**
 * JWT Token Service
 *
 * Handles JWT token generation, validation, and parsing for authentication.
 * Supports two token types:
 * - Access tokens (short-lived, 15 minutes default)
 * - Refresh tokens (long-lived, 7 days default)
 *
 * Token validity periods are configurable via application properties:
 * - app.jwt.access-validity-ms
 * - app.jwt.refresh-validity-ms
 *
 * @see com.app.boilerplate.auth.JwtAuthFilter
 */

import com.app.boilerplate.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-validity-ms}") long accessValidityMs,
            @Value("${app.jwt.refresh-validity-ms}") long refreshValidityMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    /**
     * Generate a short-lived access token for the user
     *
     * @param user User entity containing email as the subject
     * @return JWT access token string
     */
    public String generateAccessToken(User user) {
        return buildToken(user.getEmail(), accessValidityMs, TYPE_ACCESS);
    }

    /**
     * Generate a long-lived refresh token for the user
     *
     * @param user User entity containing email as the subject
     * @return JWT refresh token string
     */
    public String generateRefreshToken(User user) {
        return buildToken(user.getEmail(), refreshValidityMs, TYPE_REFRESH);
    }

    private String buildToken(String email, long validityMs, String type) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extract email (subject) from JWT token
     *
     * @param token JWT token string
     * @return Email address from token subject
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Check if token is a refresh token (vs access token)
     *
     * @param token JWT token string
     * @return true if token type is "refresh", false otherwise
     */
    public boolean isRefreshToken(String token) {
        try {
            return TYPE_REFRESH.equals(getClaims(token).get(CLAIM_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
