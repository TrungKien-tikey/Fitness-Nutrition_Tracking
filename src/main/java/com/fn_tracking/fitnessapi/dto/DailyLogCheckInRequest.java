package com.fn_tracking.fitnessapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyLogCheckInRequest {

    @NotNull(message = "Calories cannot be null")
    @Min(value = 0, message = "Calories must be greater than or equal to 0")
    private Integer caloriesIn;

    @NotNull(message = "Current weight cannot be null")
    @DecimalMin(value = "10.0", message = "Current weight must be greater than or equal to 10kg")
    private Double currentWeight;

    @PastOrPresent(message = "Log date cannot be in the future")
    private LocalDate logDate;
}