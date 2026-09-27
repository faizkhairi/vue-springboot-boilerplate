package com.app.boilerplate.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.boilerplate.auth.dto.LoginRequest;
import com.app.boilerplate.auth.dto.RegisterRequest;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret", () -> "0123456789012345678901234567890123456789012345678901234567890123");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    /**
     * There is no live SMTP server in the test environment. AuthController sends a
     * welcome email on registration, so JavaMailSender is mocked here rather than
     * letting the real implementation try (and fail) to connect to an SMTP host.
     *
     * <p>The mock is typed as the concrete JavaMailSenderImpl rather than the
     * JavaMailSender interface: Actuator's mail health contributor looks up beans by
     * the concrete JavaMailSenderImpl type and fails startup if none are found, so an
     * interface-typed mock would break the application context here.
     */
    @MockitoBean
    private JavaMailSenderImpl javaMailSender;

    @Test
    void registerThenLogin() throws Exception {
        Mockito.when(javaMailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        RegisterRequest register = new RegisterRequest();
        register.setName("Test User");
        register.setEmail("test@example.com");
        register.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setEmail("test@example.com");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("nonexistent@example.com");
        login.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityHeadersArePresentOnResponse() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("nonexistent@example.com");
        login.setPassword("wrong");

        // Spring Security only writes Strict-Transport-Security on a secure (HTTPS)
        // request, since HSTS is meaningless (and not sent by browsers) over plain
        // HTTP; .secure(true) simulates that so the assertion below is meaningful.
        mockMvc.perform(post("/api/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
                .andExpect(header().string(
                                "Content-Security-Policy",
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                                        + "img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; "
                                        + "object-src 'none'; base-uri 'self'"));
    }

    @Test
    void actuatorHealthReturnsUpAndOtherEndpointsAreNotExposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // Authenticate so the assertion below is about exposure (management.endpoints.web
        // .exposure.include=health), not about the unrelated "anyRequest().authenticated()"
        // security rule: an authenticated caller hitting a non-exposed endpoint id still
        // gets 404, because that endpoint was never registered as a servable route.
        String token = jwtService.generateAccessToken(testUser());

        mockMvc.perform(get("/actuator/env").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/beans").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private com.app.boilerplate.user.User testUser() {
        com.app.boilerplate.user.User user = new com.app.boilerplate.user.User();
        user.setEmail("actuator-probe@example.com");
        return user;
    }
}
