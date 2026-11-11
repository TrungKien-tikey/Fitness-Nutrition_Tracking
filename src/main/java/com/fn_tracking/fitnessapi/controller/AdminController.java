package com.fn_tracking.fitnessapi.controller;

import com.fn_tracking.fitnessapi.dto.AdminStatsResponse;
import com.fn_tracking.fitnessapi.dto.AdminUserResponse;
import com.fn_tracking.fitnessapi.dto.UpdateRoleRequest;
import com.fn_tracking.fitnessapi.dto.UpdateUserStatusRequest;
import com.fn_tracking.fitnessapi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Quản trị người dùng và thống kê hệ thống")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Danh sách người dùng", description = "Lấy danh sách người dùng có phân trang và tìm kiếm theo username/email")
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @Parameter(description = "Trang bắt đầu từ 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang, tối đa 100", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Từ khóa tìm kiếm theo username hoặc email")
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.getUsers(page, size, keyword));
    }

    @Operation(summary = "Đổi vai trò người dùng", description = "Cấp hoặc thu hồi quyền ADMIN")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(adminService.updateRole(id, request.getRole(), authentication.getName()));
    }

    @Operation(summary = "Khóa hoặc mở khóa tài khoản", description = "Tài khoản bị khóa không thể đăng nhập hoặc dùng API")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(adminService.updateStatus(id, request.getEnabled(), authentication.getName()));
    }

    @Operation(summary = "Thống kê hệ thống", description = "Số liệu tổng hợp về người dùng, check-in và thực đơn AI")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
