package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.ApiResponse;
import com.example.BTL_Mobile.dto.AuthResponse;
import com.example.BTL_Mobile.dto.LoginRequest;
import com.example.BTL_Mobile.dto.RefreshTokenRequest;
import com.example.BTL_Mobile.dto.RegisterRequest;
import com.example.BTL_Mobile.dto.TokenValidationResponse;
import com.example.BTL_Mobile.dto.UserResponse;
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

    /**
     * Bấm "Đăng nhập bằng Facebook" -> redirect tới trang đăng nhập Facebook.
     * Project này dành cho mobile app: login xong redirect về deep link elearn://login/callback.
     */
    @GetMapping("/oauth2/authorize/facebook")
    public void authorizeFacebook(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/facebook");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công!", response));
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
