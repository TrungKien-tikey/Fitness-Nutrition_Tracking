package com.fn_tracking.fitnessapi.dto;

import com.fn_tracking.fitnessapi.entity.Role;
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
@Schema(description = "Thông tin người dùng dành cho quản trị viên")
public class AdminUserResponse {

    @Schema(description = "ID người dùng", example = "1")
    private Long id;

    @Schema(description = "Tên đăng nhập", example = "intern_test")
    private String username;

    @Schema(description = "Email", example = "test@fitness.com")
    private String email;

    @Schema(description = "Vai trò", example = "USER")
    private Role role;

    @Schema(description = "Trạng thái tài khoản", example = "true")
    private boolean enabled;

    @Schema(description = "Thời điểm tạo tài khoản")
    private LocalDateTime createdAt;
}
