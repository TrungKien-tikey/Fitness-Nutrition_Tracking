package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu tạo thực đơn bằng AI")
public class DietPlanGenerationRequest {

    @Schema(description = "Mục tiêu thể hình", example = "WEIGHT_LOSS")
    @NotNull(message = "Goal cannot be null")
    private FitnessGoal goal;

    @Schema(description = "Ghi chú chế độ ăn (dị ứng, kiêng cữ...)", example = "Ưu tiên món ít dầu mỡ")
    private String dietaryNotes;

    @Schema(description = "Danh sách thực phẩm cần loại trừ", example = "[\"shellfish\"]")
    private List<String> excludedFoods;

    @Schema(description = "Các nền ẩm thực yêu thích", example = "[\"Asian\"]")
    private List<String> preferredCuisines;
}