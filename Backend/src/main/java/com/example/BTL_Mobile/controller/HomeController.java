package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.CourseProgressResponse;
import com.example.BTL_Mobile.dto.response.HomeResponse;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
@Tag(name = "Home", description = "API trang chủ")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "Lấy dữ liệu trang chủ",
            description = "Trả về tên người dùng, danh sách bài học đang học, danh sách khoá học và tiến độ")
    public ResponseEntity<ApiResponse<HomeResponse>> getHomeData(
            @AuthenticationPrincipal User user) {
        HomeResponse homeData = homeService.getHomeData(user);
        return ResponseEntity.ok(ApiResponse.success(homeData));
    }

    @GetMapping("/courses")
    @Operation(summary = "Lấy danh sách khoá học (topics) dạng phân trang")
    public ResponseEntity<ApiResponse<java.util.List<CourseProgressResponse>>> getCoursesPage(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        java.util.List<CourseProgressResponse> courses = homeService.getCoursesPage(user, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }
}
