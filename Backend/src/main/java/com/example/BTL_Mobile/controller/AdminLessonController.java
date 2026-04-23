package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.AdminLessonCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminLessonUpdateRequest;
import com.example.BTL_Mobile.dto.response.AdminLessonResponse;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.service.AdminLessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminLessonController {

    private final AdminLessonService adminLessonService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminLessonResponse>>> list(Pageable pageable) {
        Page<AdminLessonResponse> lessons = adminLessonService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lesson thành công!", lessons));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminLessonResponse>> getById(@PathVariable Integer id) {
        AdminLessonResponse lesson = adminLessonService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lesson thành công!", lesson));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminLessonResponse>> create(
            @Valid @RequestBody AdminLessonCreateRequest request) {
        AdminLessonResponse lesson = adminLessonService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lesson thành công!", lesson));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminLessonResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdminLessonUpdateRequest request
    ) {
        AdminLessonResponse lesson = adminLessonService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lesson thành công!", lesson));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        adminLessonService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lesson thành công!", null));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminLessonResponse>>> bulkImport(
            @RequestBody List<AdminLessonCreateRequest> requests) {
        List<AdminLessonResponse> lessons = adminLessonService.bulkImport(requests);
        return ResponseEntity.ok(ApiResponse.success("Import " + lessons.size() + " lesson thành công!", lessons));
    }
}