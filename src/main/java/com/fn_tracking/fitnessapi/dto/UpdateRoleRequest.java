package com.fn_tracking.fitnessapi.dto;

import com.fn_tracking.fitnessapi.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đổi vai trò người dùng")
public class UpdateRoleRequest {

    @Schema(description = "Vai trò mới", example = "ADMIN")
    @NotNull(message = "Role cannot be null")
    private Role role;
}
