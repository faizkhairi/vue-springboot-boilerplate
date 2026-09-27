package com.app.boilerplate.common.filter;

import com.app.boilerplate.common.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Limits requests to the authentication endpoints (/api/auth/**) to 5 requests
 * per minute, per client IP, per endpoint path.
 *
 * <p>The request counters are held in an in-memory map local to this application
 * instance. In a multi-instance deployment (multiple replicas behind a load
 * balancer), each instance enforces its own independent limit rather than a
 * shared one; deployments that need a global limit across instances must back
 * this filter with a shared store (e.g. Redis) instead.
 *
 * <p>Client IP resolution: the X-Forwarded-For header is trusted for the
 * TRUSTED_PROXY_COUNT nearest hops (the entries appended by proxies the
 * deployment controls). The client IP is the X-Forwarded-For entry
 * TRUSTED_PROXY_COUNT places from the right; any entries to the left of that
 * (including a client-supplied leftmost value) are ignored, since a caller can
 * freely set those. If X-Forwarded-For has fewer entries than
 * TRUSTED_PROXY_COUNT, the client IP cannot be determined and the request is
 * grouped into one shared "unknown" bucket. X-Real-IP is consulted only when
 * X-Forwarded-For is absent. Otherwise the filter falls back to
 * request.getRemoteAddr().
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String AUTH_PATH_PREFIX = "/api/auth/";
    private static final String UNKNOWN_CLIENT_BUCKET = "unknown";
    private static final int DEFAULT_TRUSTED_PROXY_COUNT = 1;
    private static final int SWEEP_EVERY_N_REQUESTS = 200;

    private final ObjectMapper objectMapper;
    private final int trustedProxyCount;
    private final ConcurrentMap<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public RateLimitFilter(
            ObjectMapper objectMapper, @Value("${app.security.trusted-proxy-count:1}") String trustedProxyCountRaw) {
        this.objectMapper = objectMapper;
        this.trustedProxyCount = parseTrustedProxyCount(trustedProxyCountRaw);
    }

    private static int parseTrustedProxyCount(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_TRUSTED_PROXY_COUNT;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_TRUSTED_PROXY_COUNT;
        } catch (NumberFormatException e) {
            return DEFAULT_TRUSTED_PROXY_COUNT;
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith(AUTH_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String bucketKey = clientIp + '|' + request.getRequestURI();
        Instant now = Instant.now();

        Deque<Instant> timestamps = buckets.computeIfAbsent(bucketKey, key -> new ArrayDeque<>());
        long retryAfterSeconds;
        boolean limited;
        synchronized (timestamps) {
            evictExpired(timestamps, now);
            if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                limited = true;
                retryAfterSeconds = secondsUntilOldestExpires(timestamps.peekFirst(), now);
            } else {
                limited = false;
                retryAfterSeconds = 0;
                timestamps.addLast(now);
            }
        }

        maybeSweepStaleBuckets(now);

        if (limited) {
            writeRateLimitedResponse(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        while (!timestamps.isEmpty() && isExpired(timestamps.peekFirst(), now)) {
            timestamps.pollFirst();
        }
    }

    private boolean isExpired(Instant timestamp, Instant now) {
        return Duration.between(timestamp, now).compareTo(WINDOW) >= 0;
    }

    private long secondsUntilOldestExpires(Instant oldest, Instant now) {
        if (oldest == null) {
            return 1;
        }
        long remainingMillis = WINDOW.toMillis() - Duration.between(oldest, now).toMillis();
        long seconds = (remainingMillis + 999) / 1000;
        return Math.max(1, seconds);
    }

    /**
     * Periodically removes buckets whose window has fully expired, so the map does
     * not grow without bound as distinct client/path combinations come and go.
     */
    private void maybeSweepStaleBuckets(Instant now) {
        if (requestCounter.incrementAndGet() % SWEEP_EVERY_N_REQUESTS != 0) {
            return;
        }
        buckets.entrySet().removeIf(entry -> {
            Deque<Instant> timestamps = entry.getValue();
            synchronized (timestamps) {
                evictExpired(timestamps, now);
                return timestamps.isEmpty();
            }
        });
    }

    private void writeRateLimitedResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(
                "Too many authentication attempts. Please try again in a minute.", "RATE_LIMIT_EXCEEDED");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] entries = forwardedFor.split(",");
            int count = entries.length;
            int indexFromLeft = count - trustedProxyCount;
            if (indexFromLeft < 0) {
                return UNKNOWN_CLIENT_BUCKET;
            }
            return entries[indexFromLeft].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
