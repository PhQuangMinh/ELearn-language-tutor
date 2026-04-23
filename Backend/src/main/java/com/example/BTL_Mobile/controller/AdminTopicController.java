package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.AdminTopicCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminTopicUpdateRequest;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.TopicResponse;
import com.example.BTL_Mobile.service.AdminTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminTopicController {

  private final AdminTopicService adminTopicService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<TopicResponse>>> list(Pageable pageable) {
    Page<TopicResponse> topics = adminTopicService.list(pageable);
    return ResponseEntity.ok(ApiResponse.success("Lấy danh sách topic thành công!", topics));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<TopicResponse>> getById(@PathVariable Integer id) {
    TopicResponse topic = adminTopicService.getById(id);
    return ResponseEntity.ok(ApiResponse.success("Lấy thông tin topic thành công!", topic));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<TopicResponse>> create(
      @Valid @RequestBody AdminTopicCreateRequest request) {
    TopicResponse topic = adminTopicService.create(request);
    return ResponseEntity.ok(ApiResponse.success("Tạo topic thành công!", topic));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<TopicResponse>> update(
      @PathVariable Integer id,
      @Valid @RequestBody AdminTopicUpdateRequest request
  ) {
    TopicResponse topic = adminTopicService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Cập nhật topic thành công!", topic));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
    adminTopicService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Xóa topic thành công!", null));
  }

  @PostMapping("/import")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<TopicResponse>>> bulkImport(
      @RequestBody List<AdminTopicCreateRequest> requests) {
    List<TopicResponse> topics = adminTopicService.bulkImport(requests);
    return ResponseEntity.ok(ApiResponse.success("Import " + topics.size() + " topic thành công!", topics));
  }
}
