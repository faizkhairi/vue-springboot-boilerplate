package com.app.boilerplate.user;

import com.app.boilerplate.common.util.AuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser("Test User", "test@example.com", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(auditLogger).logRegistration(anyString(), eq("test@example.com"));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("Test User", "test@example.com", "password123")
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogger).log(eq("USER_CREATION_DUPLICATE_EMAIL"), isNull(), anyMap());
    }

    @Test
    void findByEmail_UserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_UserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("notfound@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void validatePassword_CorrectPassword() {
        // Arrange
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = userService.validatePassword(testUser, "password123");

        // Assert
        assertTrue(result);
        verify(passwordEncoder).matches("password123", "hashedPassword");
    }

    @Test
    void validatePassword_IncorrectPassword() {
        // Arrange
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        boolean result = userService.validatePassword(testUser, "wrongpassword");

        // Assert
        assertFalse(result);
        verify(passwordEncoder).matches("wrongpassword", "hashedPassword");
    }

    @Test
    void updatePassword_Success() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updatePassword(testUser, "newPassword");

        // Assert
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
        verify(auditLogger).log(eq("PASSWORD_UPDATED"), eq("1"));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        doNothing().when(userRepository).deleteById(anyLong());

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
        verify(auditLogger).log(eq("USER_DELETED"), eq("1"));
    }

    @Test
    void emailExists_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        boolean result = userService.emailExists("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void emailExists_ReturnsFalse() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act
        boolean result = userService.emailExists("new@example.com");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("new@example.com");
    }
}
