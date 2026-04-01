package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.AdminScenarioCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminScenarioUpdateRequest;
import com.example.BTL_Mobile.dto.response.AdminScenarioResponse;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.service.AdminScenarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/scenarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminScenarioController {

    private final AdminScenarioService adminScenarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminScenarioResponse>>> list(Pageable pageable) {
        Page<AdminScenarioResponse> scenarios = adminScenarioService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách scenario thành công!", scenarios));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminScenarioResponse>> getById(@PathVariable Integer id) {
        AdminScenarioResponse scenario = adminScenarioService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin scenario thành công!", scenario));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminScenarioResponse>> create(
            @Valid @RequestBody AdminScenarioCreateRequest request) {
        AdminScenarioResponse scenario = adminScenarioService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo scenario thành công!", scenario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminScenarioResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdminScenarioUpdateRequest request
    ) {
        AdminScenarioResponse scenario = adminScenarioService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật scenario thành công!", scenario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        adminScenarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa scenario thành công!", null));
    }
}