package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Dữ liệu check-in dinh dưỡng hằng ngày")
public class DailyLogCheckInRequest {

    @Schema(description = "Lượng calo nạp vào", example = "2200")
    @NotNull(message = "Calories cannot be null")
    @Min(value = 0, message = "Calories must be greater than or equal to 0")
    private Integer caloriesIn;

    @Schema(description = "Cân nặng hiện tại (kg), tối thiểu 10", example = "64.5")
    @NotNull(message = "Current weight cannot be null")
    @DecimalMin(value = "10.0", message = "Current weight must be greater than or equal to 10kg")
    private Double currentWeight;

    @Schema(description = "Ngày ghi nhận (không được ở tương lai)", example = "2026-07-24")
    @PastOrPresent(message = "Log date cannot be in the future")
    private LocalDate logDate;
}