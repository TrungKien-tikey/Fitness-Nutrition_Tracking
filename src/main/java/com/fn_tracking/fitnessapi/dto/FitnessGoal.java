package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Mục tiêu thể hình")
public enum FitnessGoal {
    WEIGHT_LOSS(-400),
    MAINTENANCE(0),
    MUSCLE_GAIN(300);

    private final int calorieAdjustment;

    FitnessGoal(int calorieAdjustment) {
        this.calorieAdjustment = calorieAdjustment;
    }
}