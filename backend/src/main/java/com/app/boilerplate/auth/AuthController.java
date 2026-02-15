package com.app.boilerplate.auth;

/**
 * Authentication Controller
 *
 * Handles user authentication operations including login, registration, and JWT token refresh.
 * All endpoints return standardized error responses using {@link com.app.boilerplate.common.dto.ErrorResponse}.
 *
 * @see com.app.boilerplate.auth.JwtService
 * @see com.app.boilerplate.user.UserService
 */

import com.app.boilerplate.auth.dto.LoginRequest;
import com.app.boilerplate.auth.dto.RefreshRequest;
import com.app.boilerplate.auth.dto.RegisterRequest;
import com.app.boilerplate.auth.dto.TokenResponse;
import com.app.boilerplate.common.dto.ErrorResponse;
import com.app.boilerplate.common.util.AuditLogger;
import com.app.boilerplate.email.EmailService;
import com.app.boilerplate.user.User;
import com.app.boilerplate.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditLogger auditLogger;

    @Value("${app.url:http://localhost:5173}")
    private String appUrl;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          EmailService emailService,
                          AuditLogger auditLogger) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.auditLogger = auditLogger;
    }

    /**
     * Refresh access token using a valid refresh token
     *
     * @param request Contains the refresh token to validate
     * @return TokenResponse with new access and refresh tokens, or error response
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        String token = request.getRefreshToken();
        if (token == null || !jwtService.isTokenValid(token) || !jwtService.isRefreshToken(token)) {
            auditLogger.log("TOKEN_REFRESH_INVALID", null);
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Invalid refresh token", "UNAUTHORIZED"));
        }
        String email = jwtService.extractEmail(token);
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            auditLogger.log("TOKEN_REFRESH_USER_NOT_FOUND", null);
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("User not found", "UNAUTHORIZED"));
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        auditLogger.logTokenRefresh(user.getId().toString());
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    /**
     * Register a new user account
     *
     * Creates a new user, sends a welcome email, and returns JWT tokens for immediate authentication.
     *
     * @param request Contains name, email, and password for the new user
     * @return TokenResponse with access and refresh tokens, or error if email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword()
            );

            try {
                emailService.sendWelcome(user.getEmail(), user.getName(), appUrl);
            } catch (MessagingException ignored) {
                // Log in production; do not fail registration
            }

            String access = jwtService.generateAccessToken(user);
            String refresh = jwtService.generateRefreshToken(user);
            return ResponseEntity.ok(new TokenResponse(access, refresh));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Authenticate user with email and password
     *
     * Validates credentials and returns JWT tokens for authenticated session.
     * Logs all authentication attempts for audit purposes.
     *
     * @param request Contains email and password
     * @return TokenResponse with access and refresh tokens, or 401 error for invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !userService.validatePassword(user, request.getPassword())) {
            auditLogger.logAuthFailure(request.getEmail(), user == null ? "user_not_found" : "invalid_password");
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Invalid email or password", "INVALID_CREDENTIALS"));
        }

        auditLogger.logAuthSuccess(user.getId().toString(), "credentials");

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }
}
