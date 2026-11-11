package com.fn_tracking.fitnessapi.unit.service;

import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameShouldReturnSpringUserWhenUserExists() {
        User user = User.builder()
                .username("john")
                .password("encoded-secret")
                .role(Role.USER)
                .build();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john");

        assertEquals("john", userDetails.getUsername());
        assertEquals("encoded-secret", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameShouldReturnAdminAuthorityAndDisabledFlag() {
        User user = User.builder()
                .username("admin")
                .password("encoded-secret")
                .role(Role.ADMIN)
                .enabled(false)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost")
        );

        assertTrue(exception.getMessage().contains("ghost"));
    }
}
