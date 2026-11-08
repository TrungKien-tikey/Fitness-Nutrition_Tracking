package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thực đơn 7 ngày do AI tạo")
public class DietPlanResponse {

    @Schema(description = "ID thực đơn", example = "1")
    private Long id;

    @Schema(description = "Mục tiêu thể hình", example = "WEIGHT_LOSS")
    private FitnessGoal goal;

    @Schema(description = "Nội dung thực đơn dạng chuỗi JSON")
    private String menuJson;

    @Schema(description = "Thời điểm tạo thực đơn")
    private LocalDateTime createdAt;
}