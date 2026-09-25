package com.app.boilerplate.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the actual regression: app.jwt.secret must never resolve to a
 * hardcoded fallback again. JwtService's length check alone would not
 * catch a well-formed-but-hardcoded default being reintroduced (the
 * previous default was long enough to pass length validation), so this
 * asserts the config files themselves never grant it a default value.
 */
class JwtSecretConfigTest {

    @Test
    void applicationYmlDeclaresJwtSecretWithNoDefault() throws IOException {
        String yml = readRelativeToModuleRoot("src/main/resources/application.yml");
        assertThat(yml).contains("secret: ${JWT_SECRET}");
        assertThat(yml).doesNotContain("JWT_SECRET:your-256-bit-secret-change-in-production");
        assertThat(yml).doesNotContainPattern("secret: \\$\\{JWT_SECRET:[^}]*\\}");
    }

    @Test
    void dockerComposeRequiresJwtSecretRatherThanDefaultingIt() throws IOException {
        String compose = readRelativeToModuleRoot("../docker/docker-compose.yml");
        assertThat(compose).contains("JWT_SECRET: ${JWT_SECRET:?");
        assertThat(compose).doesNotContain("JWT_SECRET: ${JWT_SECRET:-");
    }

    private static String readRelativeToModuleRoot(String relativePath) throws IOException {
        // Gradle runs tests with the module root (backend/) as the working directory.
        return Files.readString(Path.of(relativePath));
    }
}
