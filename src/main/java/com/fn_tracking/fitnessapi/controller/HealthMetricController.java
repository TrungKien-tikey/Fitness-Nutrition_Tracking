package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.HealthMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-metrics")
@RequiredArgsConstructor
public class HealthMetricController {

    private final HealthMetricService healthMetricService;
    private final UserRepository userRepository;

    @PostMapping("/calculate")
    public ResponseEntity<HealthMetric> calculateMetrics(@Valid @RequestBody HealthMetricRequest request) {
        
        // 1. Lấy username (email) từ JWT Token của người đang request
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm User trong Database
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Truyền User vào Service
        HealthMetric savedMetric = healthMetricService.calculateAndSaveMetrics(request, currentUser);
        
        return ResponseEntity.ok(savedMetric);
    }
}