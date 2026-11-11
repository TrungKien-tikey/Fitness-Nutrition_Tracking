package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Số liệu thống kê toàn hệ thống dành cho quản trị viên")
public class AdminStatsResponse {

    @Schema(description = "Tổng số tài khoản", example = "120")
    private long totalUsers;

    @Schema(description = "Số tài khoản tạo trong 7 ngày gần nhất", example = "15")
    private long newUsersLast7Days;

    @Schema(description = "Số người dùng có check-in trong 7 ngày gần nhất", example = "40")
    private long activeUsersLast7Days;

    @Schema(description = "Số người dùng chưa check-in hôm nay", example = "80")
    private long usersMissingCheckInToday;

    @Schema(description = "Tổng số lượt check-in", example = "950")
    private long totalCheckIns;

    @Schema(description = "Số lượt check-in trong 7 ngày gần nhất", example = "210")
    private long checkInsLast7Days;

    @Schema(description = "Tổng số thực đơn AI đã tạo", example = "60")
    private long totalDietPlans;

    @Schema(description = "Số thực đơn AI tạo trong 7 ngày gần nhất", example = "18")
    private long dietPlansLast7Days;
}
