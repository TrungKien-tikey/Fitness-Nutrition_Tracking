package com.fn_tracking.fitnessapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng ký tài khoản mới")
public class RegisterRequest {

    @Schema(description = "Tên đăng nhập (3-50 ký tự)", example = "johndoe")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50)
    private String username;

    @Schema(description = "Mật khẩu (tối thiểu 6 ký tự)", example = "secret123")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6)
    private String password;

    @Schema(description = "Địa chỉ email", example = "johndoe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;
}