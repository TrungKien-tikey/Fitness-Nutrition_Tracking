package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bản ghi check-in dinh dưỡng")
public class DailyLogResponse {

    @Schema(description = "ID bản ghi", example = "1")
    private Long id;

    @Schema(description = "Ngày ghi nhận", example = "2026-07-24")
    private LocalDate logDate;

    @Schema(description = "Lượng calo nạp vào", example = "2200")
    private Integer caloriesIn;

    @Schema(description = "Cân nặng hiện tại (kg)", example = "64.5")
    private Double currentWeight;
}