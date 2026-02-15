package com.app.boilerplate.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for logging audit events (auth, security, data changes)
 */
@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("audit");

    /**
     * Log an audit event
     *
     * @param event The event name (e.g., "USER_LOGIN", "PASSWORD_RESET")
     * @param userId The user ID associated with the event (nullable)
     * @param details Additional details about the event
     */
    public void log(String event, String userId, Map<String, Object> details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", event);
        auditData.put("userId", userId);
        auditData.put("timestamp", System.currentTimeMillis());
        auditData.put("details", details);

        logger.info("[AUDIT] {} | User: {} | Details: {}", event, userId, details);
    }

    /**
     * Log an audit event with minimal data
     */
    public void log(String event, String userId) {
        log(event, userId, Map.of());
    }

    /**
     * Log authentication success
     */
    public void logAuthSuccess(String userId, String method) {
        log("AUTH_SUCCESS", userId, Map.of("method", method));
    }

    /**
     * Log authentication failure
     */
    public void logAuthFailure(String identifier, String reason) {
        log("AUTH_FAILURE", null, Map.of("identifier", identifier, "reason", reason));
    }

    /**
     * Log user registration
     */
    public void logRegistration(String userId, String email) {
        log("USER_REGISTRATION", userId, Map.of("email", email));
    }

    /**
     * Log password reset request
     */
    public void logPasswordResetRequest(String email) {
        log("PASSWORD_RESET_REQUEST", null, Map.of("email", email));
    }

    /**
     * Log password reset completion
     */
    public void logPasswordResetComplete(String userId) {
        log("PASSWORD_RESET_COMPLETE", userId, Map.of());
    }

    /**
     * Log token refresh
     */
    public void logTokenRefresh(String userId) {
        log("TOKEN_REFRESH", userId, Map.of());
    }

    /**
     * Log user logout
     */
    public void logLogout(String userId) {
        log("USER_LOGOUT", userId, Map.of());
    }
}
