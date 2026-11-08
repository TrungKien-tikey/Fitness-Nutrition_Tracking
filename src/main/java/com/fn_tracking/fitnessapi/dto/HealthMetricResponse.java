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
@Schema(description = "Chỉ số sức khỏe đã tính toán của người dùng")
public class HealthMetricResponse {

    @Schema(description = "ID bản ghi", example = "1")
    private Long id;

    @Schema(description = "Chiều cao (cm)", example = "170.0")
    private Double height;

    @Schema(description = "Cân nặng (kg)", example = "65.0")
    private Double weight;

    @Schema(description = "Chỉ số BMI", example = "22.5")
    private Double bmi;

    @Schema(description = "Tỷ lệ trao đổi chất cơ bản (BMR)", example = "1650.0")
    private Double bmr;

    @Schema(description = "Tổng năng lượng tiêu thụ mỗi ngày (TDEE)", example = "2557.5")
    private Double tdee;

    @Schema(description = "Thời điểm cập nhật gần nhất")
    private LocalDateTime updatedAt;
}
