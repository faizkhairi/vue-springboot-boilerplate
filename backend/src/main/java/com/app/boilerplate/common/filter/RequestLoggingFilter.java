package com.app.boilerplate.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filter to log all HTTP requests and responses
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Wrap request and response to cache content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // Proceed with the filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log the request
            logRequest(requestWrapper, responseWrapper, duration);

            // Copy the cached response content to the original response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();

        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        // Basic request log
        logger.info("HTTP {} {} - Status: {} - Duration: {}ms", method, fullUri, status, duration);

        // Detailed log in DEBUG mode
        if (logger.isDebugEnabled()) {
            StringBuilder headers = new StringBuilder();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", ");
            }

            logger.debug("Request Headers: {}", headers);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't log health check and actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.equals("/health");
    }
}
