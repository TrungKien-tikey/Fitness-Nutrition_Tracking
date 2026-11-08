package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Mức độ vận động hằng ngày")
public enum ActivityLevel {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725);

    private final double multiplier;

    ActivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }
}