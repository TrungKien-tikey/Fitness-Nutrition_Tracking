package com.fn_tracking.fitnessapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DietPlanGenerationRequest {

    @NotNull(message = "Goal cannot be null")
    private FitnessGoal goal;

    private String dietaryNotes;

    private List<String> excludedFoods;

    private List<String> preferredCuisines;
}