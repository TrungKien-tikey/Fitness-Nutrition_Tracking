package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.DietPlanGenerationRequest;
import com.fn_tracking.fitnessapi.dto.DietPlanResponse;
import com.fn_tracking.fitnessapi.entity.DietPlan;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import com.fn_tracking.fitnessapi.service.DietPlanService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Diet Plans", description = "Tạo và xem thực đơn 7 ngày bằng AI")
@RestController
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DietPlanController {

    private final DietPlanService dietPlanService;
    private final UserRepository userRepository;

    @Operation(summary = "Tạo thực đơn bằng AI", description = "Sinh thực đơn 7 ngày dựa trên mục tiêu và sở thích ăn uống")
    @PostMapping("/generate")
    public ResponseEntity<DietPlanResponse> generate(@Valid @RequestBody DietPlanGenerationRequest request) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(toResponse(dietPlanService.generateAndSaveWeeklyPlan(currentUser, request)));
    }

    @Operation(summary = "Xem thực đơn mới nhất", description = "Lấy thực đơn gần nhất của người dùng hiện tại")
    @GetMapping("/latest")
    public ResponseEntity<DietPlanResponse> getLatest() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(toResponse(dietPlanService.getLatestPlan(currentUser.getId())));
    }

    private User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private DietPlanResponse toResponse(DietPlan dietPlan) {
        return DietPlanResponse.builder()
                .id(dietPlan.getId())
                .goal(dietPlan.getGoal())
                .menuJson(dietPlan.getMenuJson())
                .createdAt(dietPlan.getCreatedAt())
                .build();
    }
}