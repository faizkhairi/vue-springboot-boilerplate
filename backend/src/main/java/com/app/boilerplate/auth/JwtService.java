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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // 256 bits, the minimum HS256 requires and the value this boilerplate documents.
    static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-validity-ms}") long accessValidityMs,
            @Value("${app.jwt.refresh-validity-ms}") long refreshValidityMs) {
        validateSecret(secret);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    /**
     * Fails fast at startup if app.jwt.secret (JWT_SECRET) is missing, blank,
     * or too short to sign HS256 tokens with. There is deliberately no
     * fallback default: a silently-active default secret is a token
     * forgery risk in any environment that forgets to set the env var.
     */
    static void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) is not set. Generate one with: openssl rand -base64 48");
        }
        int byteLength = secret.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES
                    + " bytes (256 bits) for HS256, but got " + byteLength
                    + " bytes. Generate one with: openssl rand -base64 48");
        }
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
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
