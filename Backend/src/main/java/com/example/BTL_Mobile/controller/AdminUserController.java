package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.AdminUserCreateRequest;
import com.example.BTL_Mobile.dto.AdminUserUpdateRequest;
import com.example.BTL_Mobile.dto.ApiResponse;
import com.example.BTL_Mobile.dto.UserResponse;
import com.example.BTL_Mobile.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> list(Pageable pageable) {
        Page<UserResponse> users = adminUserService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách user thành công!", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Integer id) {
        UserResponse user = adminUserService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin user thành công!", user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody AdminUserCreateRequest request) {
        UserResponse user = adminUserService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo user thành công!", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        UserResponse user = adminUserService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật user thành công!", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        adminUserService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa user thành công!", null));
    }
}
