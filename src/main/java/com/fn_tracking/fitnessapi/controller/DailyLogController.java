package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.DailyLogCheckInRequest;
import com.fn_tracking.fitnessapi.dto.DailyLogResponse;
import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.DailyLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;
    private final UserRepository userRepository;

    @PostMapping("/check-in")
    public ResponseEntity<DailyLogResponse> checkIn(@Valid @RequestBody DailyLogCheckInRequest request) {
        User currentUser = getCurrentUser();
        DailyLog savedLog = dailyLogService.checkIn(request, currentUser);
        return ResponseEntity.ok(toResponse(savedLog));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyLogResponse>> getHistory(@RequestParam(defaultValue = "7") Integer days) {
        User currentUser = getCurrentUser();
        List<DailyLogResponse> responses = dailyLogService.getRecentLogs(currentUser.getId(), days).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private DailyLogResponse toResponse(DailyLog log) {
        return DailyLogResponse.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .caloriesIn(log.getCaloriesIn())
                .currentWeight(log.getCurrentWeight())
                .build();
    }
}