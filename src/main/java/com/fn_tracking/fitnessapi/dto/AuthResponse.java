package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Thông tin xác thực trả về sau khi đăng nhập thành công")
public class AuthResponse {

    @Schema(description = "JWT token dùng cho các request cần xác thực")
    private String token;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "johndoe@example.com")
    private String email;
}