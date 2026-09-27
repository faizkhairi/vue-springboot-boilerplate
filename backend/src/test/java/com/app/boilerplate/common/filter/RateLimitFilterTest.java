package com.app.boilerplate.common.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class RateLimitFilterTest {

    private static final String PATH = "/api/auth/login";

    private RateLimitFilter newFilter(String trustedProxyCountRaw) {
        return new RateLimitFilter(JsonMapper.builder().build(), trustedProxyCountRaw);
    }

    private FilterChain countingChain(AtomicInteger callCount) {
        return (request, response) -> callCount.incrementAndGet();
    }

    @Test
    void requestsUnderTheLimitAllPassThrough() throws Exception {
        RateLimitFilter filter = newFilter("1");
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = authRequest("203.0.113.10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, countingChain(calls));
            assertThat(response.getStatus()).isEqualTo(200);
        }

        assertThat(calls.get()).isEqualTo(5);
    }

    @Test
    void sixthRequestInTheWindowIsRejectedWith429AndRetryAfter() throws Exception {
        RateLimitFilter filter = newFilter("1");
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 5; i++) {
            filter.doFilter(authRequest("203.0.113.11"), new MockHttpServletResponse(), countingChain(calls));
        }

        MockHttpServletResponse sixth = new MockHttpServletResponse();
        filter.doFilter(authRequest("203.0.113.11"), sixth, countingChain(calls));

        assertThat(sixth.getStatus()).isEqualTo(429);
        assertThat(sixth.getHeader("Retry-After")).isNotNull();
        assertThat(Long.parseLong(sixth.getHeader("Retry-After"))).isBetween(1L, 60L);
        assertThat(sixth.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
        assertThat(calls.get()).isEqualTo(5);
    }

    @Test
    void nonAuthPathsAreNeverRateLimited() throws Exception {
        RateLimitFilter filter = newFilter("1");
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
            request.addHeader("X-Forwarded-For", "203.0.113.12");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, countingChain(calls));
            assertThat(response.getStatus()).isEqualTo(200);
        }

        assertThat(calls.get()).isEqualTo(10);
    }

    @Test
    void defaultTrustedProxyCountOfOneTakesTheRightmostForwardedForEntry() {
        RateLimitFilter filter = newFilter("1");
        MockHttpServletRequest request = authRequest(null);
        request.addHeader("X-Forwarded-For", "198.51.100.1, 203.0.113.99");

        assertThat(filter.resolveClientIp(request)).isEqualTo("203.0.113.99");
    }

    @Test
    void spoofedLeftmostForwardedForEntryCannotRotateTheBucket() throws Exception {
        RateLimitFilter filter = newFilter("1");
        AtomicInteger calls = new AtomicInteger();

        // Same real (rightmost) client IP on every call, but the attacker-controlled
        // leftmost entry changes each time. This must still land in the same bucket.
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
            request.addHeader("X-Forwarded-For", "spoofed-" + i + ", 203.0.113.50");
            filter.doFilter(request, new MockHttpServletResponse(), countingChain(calls));
        }

        MockHttpServletRequest sixth = new MockHttpServletRequest("POST", PATH);
        sixth.addHeader("X-Forwarded-For", "spoofed-6, 203.0.113.50");
        MockHttpServletResponse sixthResponse = new MockHttpServletResponse();
        filter.doFilter(sixth, sixthResponse, countingChain(calls));

        assertThat(sixthResponse.getStatus()).isEqualTo(429);
        assertThat(calls.get()).isEqualTo(5);
    }

    @Test
    void trustedProxyCountOfTwoTakesTheSecondEntryFromTheRight() {
        RateLimitFilter filter = newFilter("2");
        MockHttpServletRequest request = authRequest(null);
        request.addHeader("X-Forwarded-For", "evil-client-claim, 203.0.113.77, 10.0.0.5");

        assertThat(filter.resolveClientIp(request)).isEqualTo("203.0.113.77");
    }

    @Test
    void fewerForwardedForHopsThanTrustedProxyCountFallsIntoTheSharedUnknownBucket() {
        RateLimitFilter filter = newFilter("2");
        MockHttpServletRequest request = authRequest(null);
        request.addHeader("X-Forwarded-For", "203.0.113.77");

        assertThat(filter.resolveClientIp(request)).isEqualTo("unknown");
    }

    @Test
    void invalidTrustedProxyCountFallsBackToOne() {
        RateLimitFilter filter = newFilter("not-a-number");
        MockHttpServletRequest request = authRequest(null);
        request.addHeader("X-Forwarded-For", "198.51.100.1, 203.0.113.99");

        assertThat(filter.resolveClientIp(request)).isEqualTo("203.0.113.99");
    }

    @Test
    void trustedProxyCountOfZeroIgnoresForwardingHeadersAndUsesTheSocketAddress() throws Exception {
        RateLimitFilter filter = newFilter("0");

        MockHttpServletRequest withHeaders = authRequest("203.0.113.99");
        withHeaders.addHeader("X-Real-IP", "203.0.113.200");
        withHeaders.setRemoteAddr("192.0.2.5");
        assertThat(filter.resolveClientIp(withHeaders)).isEqualTo("192.0.2.5");

        // A directly exposed API must not let a caller rotate buckets by
        // inventing a new X-Forwarded-For value on every request.
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = authRequest("198.51.100." + i);
            request.setRemoteAddr("192.0.2.5");
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        }
        MockHttpServletRequest sixth = authRequest("198.51.100.250");
        sixth.setRemoteAddr("192.0.2.5");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(sixth, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void negativeTrustedProxyCountFallsBackToOne() {
        RateLimitFilter filter = newFilter("-2");
        MockHttpServletRequest request = authRequest("198.51.100.1, 203.0.113.99");

        assertThat(filter.resolveClientIp(request)).isEqualTo("203.0.113.99");
    }

    @Test
    void xRealIpIsUsedOnlyWhenForwardedForIsAbsent() {
        RateLimitFilter filter = newFilter("1");

        MockHttpServletRequest withBoth = authRequest(null);
        withBoth.addHeader("X-Forwarded-For", "203.0.113.99");
        withBoth.addHeader("X-Real-IP", "203.0.113.200");
        assertThat(filter.resolveClientIp(withBoth)).isEqualTo("203.0.113.99");

        MockHttpServletRequest realIpOnly = authRequest(null);
        realIpOnly.addHeader("X-Real-IP", "203.0.113.200");
        assertThat(filter.resolveClientIp(realIpOnly)).isEqualTo("203.0.113.200");
    }

    @Test
    void fallsBackToRemoteAddrWhenNoForwardingHeadersArePresent() {
        RateLimitFilter filter = newFilter("1");
        MockHttpServletRequest request = authRequest(null);
        request.setRemoteAddr("192.0.2.5");

        assertThat(filter.resolveClientIp(request)).isEqualTo("192.0.2.5");
    }

    private MockHttpServletRequest authRequest(String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        return request;
    }
}
