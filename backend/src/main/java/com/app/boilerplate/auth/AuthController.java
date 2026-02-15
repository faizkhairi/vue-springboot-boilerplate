package com.app.boilerplate.auth;

import com.app.boilerplate.auth.dto.LoginRequest;
import com.app.boilerplate.auth.dto.RefreshRequest;
import com.app.boilerplate.auth.dto.RegisterRequest;
import com.app.boilerplate.auth.dto.TokenResponse;
import com.app.boilerplate.email.EmailService;
import com.app.boilerplate.user.User;
import com.app.boilerplate.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.url:http://localhost:5173}")
    private String appUrl;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        String token = request.getRefreshToken();
        if (token == null || !jwtService.isTokenValid(token) || !jwtService.isRefreshToken(token)) {
            return ResponseEntity.status(401).build();
        }
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);
        try {
            emailService.sendWelcome(user.getEmail(), user.getName(), appUrl);
        } catch (MessagingException ignored) {
            // Log in production; do not fail registration
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).build();
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }
}
