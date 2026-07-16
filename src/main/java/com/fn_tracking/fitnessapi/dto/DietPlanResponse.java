package com.fn_tracking.fitnessapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietPlanResponse {
    private Long id;
    private FitnessGoal goal;
    private String menuJson;
    private LocalDateTime createdAt;
}