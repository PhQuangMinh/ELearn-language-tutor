package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.CourseProgressResponse;
import com.example.BTL_Mobile.dto.response.HomeResponse;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        HomeResponse raw = homeService.getHomeData(user);
        HomeResponse homeData = HomeResponse.builder()
                .fullName(raw.getFullName())
                .courses(resolveImageUrls(raw.getCourses(), request))
                .build();
        return ResponseEntity.ok(ApiResponse.success(homeData));
    }

    @GetMapping("/courses")
    @Operation(summary = "Lấy danh sách khoá học (topics) dạng phân trang")
    public ResponseEntity<ApiResponse<java.util.List<CourseProgressResponse>>> getCoursesPage(
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CourseProgressResponse> raw = homeService.getCoursesPage(user, page, size);
        List<CourseProgressResponse> courses = resolveImageUrls(raw, request);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping(value = "/topic-default-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getTopicDefaultImage() {
        Path candidate1 = Path.of("Backend", "images.png");
        Path candidate2 = Path.of("images.png");
        Path target = Files.exists(candidate1) ? candidate1 : candidate2;
        return ResponseEntity.ok(new FileSystemResource(target.toFile()));
    }

    private List<CourseProgressResponse> resolveImageUrls(
            List<CourseProgressResponse> courses,
            HttpServletRequest request
    ) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String defaultUrl = baseUrl + "/api/home/topic-default-image";

        return courses.stream()
                .map(c -> CourseProgressResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .progressPercent(c.getProgressPercent())
                        .imageUrl(normalizeImageUrl(c.getImageUrl(), baseUrl, defaultUrl))
                        .build())
                .toList();
    }

    private String normalizeImageUrl(String raw, String baseUrl, String defaultUrl) {
        if (raw == null || raw.isBlank()) {
            return defaultUrl;
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        if (raw.startsWith("/")) {
            return baseUrl + raw;
        }
        return baseUrl + "/" + raw;
    }
}
