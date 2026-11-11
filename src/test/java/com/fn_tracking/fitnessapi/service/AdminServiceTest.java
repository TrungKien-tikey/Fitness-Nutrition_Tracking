package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.AdminStatsResponse;
import com.fn_tracking.fitnessapi.dto.AdminUserResponse;
import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import com.fn_tracking.fitnessapi.repository.DietPlanRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyLogRepository dailyLogRepository;

    @Mock
    private DietPlanRepository dietPlanRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getUsersShouldReturnMappedPage() {
        User user = user(1L, "john", Role.USER, true);
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<AdminUserResponse> result = adminService.getUsers(-1, 500, "  ");

        assertEquals(1, result.getTotalElements());
        assertEquals("john", result.getContent().get(0).getUsername());
        assertEquals(Role.USER, result.getContent().get(0).getRole());
        verify(userRepository).findAll(PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("id").ascending()));
    }

    @Test
    void getUsersShouldSearchByKeyword() {
        User user = user(1L, "john", Role.USER, true);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                eq("jo"), eq("jo"), any(Pageable.class))).thenReturn(page);

        Page<AdminUserResponse> result = adminService.getUsers(0, 10, " jo ");

        assertEquals(1, result.getTotalElements());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateRoleShouldChangeRole() {
        User user = user(2L, "intern", Role.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserResponse response = adminService.updateRole(2L, Role.ADMIN, "admin");

        assertEquals(Role.ADMIN, response.getRole());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void updateRoleShouldRejectChangingOwnRole() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateRole(1L, Role.USER, "admin"));

        assertEquals("You cannot change your own role", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRoleShouldRejectRemovingLastAdmin() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateRole(1L, Role.USER, "other-admin"));

        assertEquals("Cannot remove the last admin", exception.getMessage());
    }

    @Test
    void updateStatusShouldLockUser() {
        User user = user(2L, "intern", Role.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserResponse response = adminService.updateStatus(2L, false, "admin");

        assertFalse(response.isEnabled());
        assertFalse(user.isEnabled());
    }

    @Test
    void updateStatusShouldRejectChangingOwnStatus() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateStatus(1L, false, "admin"));

        assertEquals("You cannot change your own account status", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateStatusShouldRejectLockingLastAdmin() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateStatus(1L, false, "other-admin"));

        assertEquals("Cannot lock the last admin", exception.getMessage());
    }

    @Test
    void getStatsShouldAggregateCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(3L);
        when(dailyLogRepository.countDistinctUsersSince(any(LocalDate.class))).thenReturn(6L);
        when(dailyLogRepository.countDistinctUsersByLogDate(any(LocalDate.class))).thenReturn(4L);
        when(dailyLogRepository.count()).thenReturn(50L);
        when(dailyLogRepository.countByLogDateGreaterThanEqual(any(LocalDate.class))).thenReturn(20L);
        when(dietPlanRepository.count()).thenReturn(8L);
        when(dietPlanRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(2L);

        AdminStatsResponse stats = adminService.getStats();

        assertEquals(10L, stats.getTotalUsers());
        assertEquals(3L, stats.getNewUsersLast7Days());
        assertEquals(6L, stats.getActiveUsersLast7Days());
        assertEquals(6L, stats.getUsersMissingCheckInToday());
        assertEquals(50L, stats.getTotalCheckIns());
        assertEquals(20L, stats.getCheckInsLast7Days());
        assertEquals(8L, stats.getTotalDietPlans());
        assertEquals(2L, stats.getDietPlansLast7Days());
    }

    @Test
    void getStatsShouldNotReturnNegativeMissingCheckIns() {
        when(userRepository.count()).thenReturn(2L);
        when(dailyLogRepository.countDistinctUsersByLogDate(any(LocalDate.class))).thenReturn(5L);

        AdminStatsResponse stats = adminService.getStats();

        assertEquals(0L, stats.getUsersMissingCheckInToday());
    }

    private User user(Long id, String username, Role role, boolean enabled) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .password("encoded-secret")
                .role(role)
                .enabled(enabled)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
