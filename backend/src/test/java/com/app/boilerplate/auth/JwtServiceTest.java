package com.app.boilerplate.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Regression coverage for the JWT_SECRET fail-fast validation: this
 * boilerplate must never silently start up with a default/weak secret.
 */
class JwtServiceTest {

    private static final String VALID_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123"; // 66 bytes

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingOrBlankSecret(String secret) {
        assertThatIllegalStateException()
                .isThrownBy(() -> JwtService.validateSecret(secret))
                .withMessageContaining("JWT_SECRET");
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        String tooShort = "short-secret"; // 12 bytes
        assertThatIllegalStateException()
                .isThrownBy(() -> JwtService.validateSecret(tooShort))
                .withMessageContaining("32")
                .withMessageContaining("JWT_SECRET");
    }

    @Test
    void rejectsFormerDefaultSecretIfItIsEverReintroduced() {
        // The literal default this boilerplate used to ship with. It is
        // only 41 bytes but the point of this test is to lock in that no
        // hardcoded value is ever accepted implicitly again, regardless
        // of its length: it must go through the same length validation
        // as any other secret and must never be a hardcoded fallback.
        String formerDefault = "your-256-bit-secret-change-in-production";
        assertThat(formerDefault.getBytes().length).isLessThan(JwtService.MIN_SECRET_BYTES);
        assertThatIllegalStateException()
                .isThrownBy(() -> JwtService.validateSecret(formerDefault));
    }

    @Test
    void acceptsASecretOfAtLeast32Bytes() {
        assertThat(VALID_SECRET.getBytes().length).isGreaterThanOrEqualTo(JwtService.MIN_SECRET_BYTES);
        // Should not throw.
        JwtService.validateSecret(VALID_SECRET);
    }

    @Test
    void constructingWithAValidSecretGeneratesAndValidatesTokens() {
        JwtService jwtService = new JwtService(VALID_SECRET, 900_000L, 604_800_000L);
        var user = new com.app.boilerplate.user.User();
        user.setEmail("test@example.com");

        String accessToken = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(accessToken)).isTrue();
        assertThat(jwtService.extractEmail(accessToken)).isEqualTo("test@example.com");
        assertThat(jwtService.isRefreshToken(accessToken)).isFalse();
    }

    @Test
    void constructingWithAnInvalidSecretFailsFast() {
        assertThatIllegalStateException()
                .isThrownBy(() -> new JwtService("too-short", 900_000L, 604_800_000L));
    }
}
