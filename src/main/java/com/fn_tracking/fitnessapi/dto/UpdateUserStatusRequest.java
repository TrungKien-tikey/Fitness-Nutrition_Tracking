package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu khóa hoặc mở khóa tài khoản")
public class UpdateUserStatusRequest {

    @Schema(description = "true để mở khóa, false để khóa tài khoản", example = "false")
    @NotNull(message = "Enabled cannot be null")
    private Boolean enabled;
}
