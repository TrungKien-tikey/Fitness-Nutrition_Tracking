package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dữ liệu đầu vào để tính chỉ số sức khỏe")
public class HealthMetricRequest {

    @Schema(description = "Chiều cao (cm), tối thiểu 50", example = "170.0")
    @NotNull(message = "Height cannot be null")
    @Min(value = 50, message = "Height must be greater than 50cm")
    private Double height;

    @Schema(description = "Cân nặng (kg), tối thiểu 10", example = "65.0")
    @NotNull(message = "Weight cannot be null")
    @Min(value = 10, message = "Weight must be greater than 10kg")
    private Double weight;

    @Schema(description = "Tuổi, tối thiểu 1", example = "25")
    @NotNull(message = "Age cannot be null")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;

    @Schema(description = "Giới tính", example = "MALE", allowableValues = {"MALE", "FEMALE"})
    @NotBlank(message = "Gender cannot be blank (MALE/FEMALE)")
    private String gender;

    @Schema(description = "Mức độ vận động", example = "MODERATELY_ACTIVE")
    @NotNull(message = "Activity level cannot be null")
    private ActivityLevel activityLevel;
}