package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(description = "Mật khẩu", example = "secret123")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}