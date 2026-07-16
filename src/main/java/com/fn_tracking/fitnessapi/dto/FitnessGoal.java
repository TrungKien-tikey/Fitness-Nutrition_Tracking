package com.fn_tracking.fitnessapi.dto;

import lombok.Getter;

@Getter
public enum FitnessGoal {
    WEIGHT_LOSS(-400),
    MAINTENANCE(0),
    MUSCLE_GAIN(300);

    private final int calorieAdjustment;

    FitnessGoal(int calorieAdjustment) {
        this.calorieAdjustment = calorieAdjustment;
    }
}