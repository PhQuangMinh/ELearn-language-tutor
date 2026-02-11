package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.ForgotPasswordRequest;
import com.example.BTL_Mobile.dto.request.LoginRequest;
import com.example.BTL_Mobile.dto.request.RefreshTokenRequest;
import com.example.BTL_Mobile.dto.request.RegisterRequest;
import com.example.BTL_Mobile.dto.request.ResetPasswordRequest;
import com.example.BTL_Mobile.dto.request.VerifyEmailRequest;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.AuthResponse;
import com.example.BTL_Mobile.dto.response.TokenValidationResponse;
import com.example.BTL_Mobile.dto.response.UserResponse;
import com.example.BTL_Mobile.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Bấm "Đăng nhập bằng Google" -> redirect tới trang đăng nhập Google.
     * Project này dành cho mobile app: login xong redirect về deep link elearn://login/callback.
     */
    @GetMapping("/oauth2/authorize/google")
    public void authorizeGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công! Vui lòng kiểm tra email để lấy mã xác thực.", null));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công!", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công!", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Lấy access token từ header nếu có
        String accessToken = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : null;
        
        authService.logout(request.getRefreshToken(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công!", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Nếu email tồn tại, mã đặt lại mật khẩu đã được gửi.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công!", null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : null;
        
        if (token == null) {
            TokenValidationResponse response = TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token không được cung cấp")
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Kết quả xác thực token", response));
        }
        
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success("Kết quả xác thực token", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công!", response));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("Auth API đang hoạt động!", null));
    }
}
