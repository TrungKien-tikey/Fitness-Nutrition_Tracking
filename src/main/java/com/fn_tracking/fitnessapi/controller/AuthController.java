package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.AuthResponse;
import com.fn_tracking.fitnessapi.dto.LoginRequest;
import com.fn_tracking.fitnessapi.dto.RegisterRequest;
import com.fn_tracking.fitnessapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}