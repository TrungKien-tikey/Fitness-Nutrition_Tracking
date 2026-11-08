package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.AuthResponse;
import com.fn_tracking.fitnessapi.dto.LoginRequest;
import com.fn_tracking.fitnessapi.dto.RegisterRequest;
import com.fn_tracking.fitnessapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Đăng ký và đăng nhập")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản", description = "Tạo người dùng mới với username, email và mật khẩu")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Đăng nhập", description = "Xác thực thông tin đăng nhập và trả về JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}