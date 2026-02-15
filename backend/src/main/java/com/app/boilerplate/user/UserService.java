package com.app.boilerplate.user;

import com.app.boilerplate.common.util.AuditLogger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User business logic service
 *
 * Handles user-related operations separate from HTTP controllers
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create a new user
     *
     * @param name User's full name
     * @param email User's email address
     * @param password Plain text password (will be hashed)
     * @return Created user
     * @throws IllegalArgumentException if email already exists
     */
    public User createUser(String name, String email, String password) {
        if (emailExists(email)) {
            auditLogger.log("USER_CREATION_DUPLICATE_EMAIL", null, java.util.Map.of("email", email));
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        auditLogger.logRegistration(savedUser.getId().toString(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * Validate user password
     *
     * @param user User entity
     * @param rawPassword Plain text password to validate
     * @return true if password matches
     */
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * Update user password
     *
     * @param user User entity
     * @param newPassword New plain text password
     */
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogger.log("PASSWORD_UPDATED", user.getId().toString());
    }

    /**
     * Delete user
     *
     * @param userId User ID to delete
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        auditLogger.log("USER_DELETED", userId.toString());
    }
}
