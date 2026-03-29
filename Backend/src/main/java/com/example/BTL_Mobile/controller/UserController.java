package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.UserProfileResponse;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
@Tag(name = "User", description = "API thông tin hồ sơ người dùng")
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me/profile")
    @Operation(summary = "Lấy hồ sơ người dùng", description = "Trả về fullName, email, avatarUrl của user hiện tại")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse response = userProfileService.getCurrentUserProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ người dùng thành công!", response));
    }

    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật hồ sơ người dùng", description = "Cập nhật fullName và avatar (upload Cloudinary)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
        @AuthenticationPrincipal User user,
        @RequestParam("fullName") String fullName,
        @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserProfileResponse response = userProfileService.updateCurrentUserProfile(user.getId(), fullName, avatar);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ người dùng thành công!", response));
    }

    @PostMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật hồ sơ người dùng (POST fallback)", description = "Fallback cho client không parse được multipart với PUT")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfilePost(
        @AuthenticationPrincipal User user,
        @RequestParam("fullName") String fullName,
        @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserProfileResponse response = userProfileService.updateCurrentUserProfile(user.getId(), fullName, avatar);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ người dùng thành công!", response));
    }
}
