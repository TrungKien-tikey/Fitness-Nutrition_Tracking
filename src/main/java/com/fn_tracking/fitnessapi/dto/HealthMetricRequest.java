package com.fn_tracking.fitnessapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthMetricRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Height cannot be null")
    @Min(value = 50, message = "Height must be greater than 50cm")
    private Double height;

    @NotNull(message = "Weight cannot be null")
    @Min(value = 10, message = "Weight must be greater than 10kg")
    private Double weight;

    @NotNull(message = "Age cannot be null")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;

    @NotBlank(message = "Gender cannot be blank (MALE/FEMALE)")
    private String gender;

    @NotBlank(message = "Activity level cannot be blank")
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE
}