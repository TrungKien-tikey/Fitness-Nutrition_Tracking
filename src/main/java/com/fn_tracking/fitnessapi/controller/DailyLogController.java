package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.DailyLogCheckInRequest;
import com.fn_tracking.fitnessapi.dto.DailyLogResponse;
import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Daily Logs", description = "Check-in dinh dưỡng và xem lịch sử theo ngày")
@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DailyLogController {

    private final DailyLogService dailyLogService;
    private final UserRepository userRepository;

    @Operation(summary = "Check-in hằng ngày", description = "Ghi nhận lượng calo nạp vào và cân nặng hiện tại")
    @PostMapping("/check-in")
    public ResponseEntity<DailyLogResponse> checkIn(@Valid @RequestBody DailyLogCheckInRequest request) {
        User currentUser = getCurrentUser();
        DailyLog savedLog = dailyLogService.checkIn(request, currentUser);
        return ResponseEntity.ok(toResponse(savedLog));
    }

    @Operation(summary = "Lịch sử check-in", description = "Lấy các bản ghi check-in gần đây của người dùng hiện tại")
    @GetMapping("/history")
    public ResponseEntity<List<DailyLogResponse>> getHistory(
            @Parameter(description = "Số ngày gần nhất cần lấy", example = "7")
            @RequestParam(defaultValue = "7") Integer days) {
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