package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.AuthResponse;
import com.fn_tracking.fitnessapi.dto.LoginRequest;
import com.fn_tracking.fitnessapi.dto.RegisterRequest;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 86400000L);
        authService = new AuthService(userRepository, tokenProvider, passwordEncoder);
    }

    @Test
    void registerShouldSaveUserWhenUsernameAndEmailAreAvailable() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("secret123");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = authService.register(request);

        assertEquals("User registered successfully!", result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john", userCaptor.getValue().getUsername());
        assertEquals("john@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-secret", userCaptor.getValue().getPassword());
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("secret123");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginShouldReturnAuthResponseWhenPasswordMatches() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret123");

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded-secret")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertEquals("john", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertTrue(response.getToken().startsWith("eyJ"));
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrong-password");

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-secret")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid username or password", exception.getMessage());
    }
}