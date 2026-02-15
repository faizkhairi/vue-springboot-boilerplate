package com.app.boilerplate.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard error response format for all API endpoints.
 *
 * This ensures consistent error handling across the entire application,
 * making it easier for frontend clients to parse and display errors.
 *
 * Format:
 * {
 *   "error": {
 *     "message": "Human-readable error message",
 *     "code": "ERROR_CODE",  // optional
 *     "details": { }         // optional
 *   }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private ErrorDetails error;

    public ErrorResponse(String message) {
        this.error = new ErrorDetails(message);
    }

    public ErrorResponse(String message, String code) {
        this.error = new ErrorDetails(message, code);
    }

    public ErrorResponse(String message, String code, Object details) {
        this.error = new ErrorDetails(message, code, details);
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    /**
     * Nested error details object
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String message;
        private String code;
        private Object details;

        public ErrorDetails(String message) {
            this.message = message;
        }

        public ErrorDetails(String message, String code) {
            this.message = message;
            this.code = code;
        }

        public ErrorDetails(String message, String code, Object details) {
            this.message = message;
            this.code = code;
            this.details = details;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }
    }
}
