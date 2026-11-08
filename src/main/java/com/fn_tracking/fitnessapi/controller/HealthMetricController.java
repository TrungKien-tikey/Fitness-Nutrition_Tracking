package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.dto.HealthMetricResponse;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.HealthMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Health Metrics", description = "Tính toán và lưu chỉ số BMI, BMR, TDEE")
@RestController
@RequestMapping("/api/health-metrics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HealthMetricController {

    private final HealthMetricService healthMetricService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Tính chỉ số sức khỏe",
            description = "Tính BMI, BMR, TDEE từ chiều cao, cân nặng, tuổi, giới tính và mức vận động của người dùng hiện tại")
    @PostMapping("/calculate")
    public ResponseEntity<HealthMetricResponse> calculateMetrics(@Valid @RequestBody HealthMetricRequest request) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HealthMetric savedMetric = healthMetricService.calculateAndSaveMetrics(request, currentUser);

        return ResponseEntity.ok(toResponse(savedMetric));
    }

    private HealthMetricResponse toResponse(HealthMetric metric) {
        return HealthMetricResponse.builder()
                .id(metric.getId())
                .height(metric.getHeight())
                .weight(metric.getWeight())
                .bmi(metric.getBmi())
                .bmr(metric.getBmr())
                .tdee(metric.getTdee())
                .updatedAt(metric.getUpdatedAt())
                .build();
    }
}
