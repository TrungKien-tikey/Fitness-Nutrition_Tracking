package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.service.HealthMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-metrics")
@RequiredArgsConstructor
public class HealthMetricController {

    private final HealthMetricService healthMetricService;

    @PostMapping("/calculate")
    public ResponseEntity<HealthMetric> calculateMetrics(@Valid @RequestBody HealthMetricRequest request) {
        HealthMetric savedMetric = healthMetricService.calculateAndSaveMetrics(request);
        return ResponseEntity.ok(savedMetric);
    }
}