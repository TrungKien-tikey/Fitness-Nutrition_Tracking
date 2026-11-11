package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.AdminStatsResponse;
import com.fn_tracking.fitnessapi.dto.AdminUserResponse;
import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import com.fn_tracking.fitnessapi.repository.DietPlanRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DailyLogRepository dailyLogRepository;
    private final DietPlanRepository dietPlanRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(int page, int size, String keyword) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());

        String search = keyword == null ? "" : keyword.trim();
        Page<User> users = search.isEmpty()
                ? userRepository.findAll(pageable)
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);

        return users.map(this::toResponse);
    }

    @Transactional
    public AdminUserResponse updateRole(Long userId, Role role, String currentUsername) {
        User user = findUser(userId);

        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("You cannot change your own role");
        }
        Role currentRole = user.getRole() == null ? Role.USER : user.getRole();
        if (currentRole == Role.ADMIN && role != Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new RuntimeException("Cannot remove the last admin");
        }

        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateStatus(Long userId, boolean enabled, String currentUsername) {
        User user = findUser(userId);

        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("You cannot change your own account status");
        }
        Role currentRole = user.getRole() == null ? Role.USER : user.getRole();
        if (!enabled && currentRole == Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new RuntimeException("Cannot lock the last admin");
        }

        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(6);
        LocalDateTime fromTime = LocalDateTime.now().minusDays(7);

        long totalUsers = userRepository.count();
        long checkedInToday = dailyLogRepository.countDistinctUsersByLogDate(today);

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .newUsersLast7Days(userRepository.countByCreatedAtAfter(fromTime))
                .activeUsersLast7Days(dailyLogRepository.countDistinctUsersSince(fromDate))
                .usersMissingCheckInToday(Math.max(0, totalUsers - checkedInToday))
                .totalCheckIns(dailyLogRepository.count())
                .checkInsLast7Days(dailyLogRepository.countByLogDateGreaterThanEqual(fromDate))
                .totalDietPlans(dietPlanRepository.count())
                .dietPlansLast7Days(dietPlanRepository.countByCreatedAtAfter(fromTime))
                .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() == null ? Role.USER : user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
