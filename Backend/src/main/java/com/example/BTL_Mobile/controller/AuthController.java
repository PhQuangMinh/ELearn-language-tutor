package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.ForgotPasswordRequest;
import com.example.BTL_Mobile.dto.request.ChangePasswordRequest;
import com.example.BTL_Mobile.dto.request.GoogleIdTokenLoginRequest;
import com.example.BTL_Mobile.dto.request.LoginRequest;
import com.example.BTL_Mobile.dto.request.PasswordStrengthRequest;
import com.example.BTL_Mobile.dto.request.RefreshTokenRequest;
import com.example.BTL_Mobile.dto.request.RegisterCompleteRequest;
import com.example.BTL_Mobile.dto.request.RegisterInitiateRequest;
import com.example.BTL_Mobile.dto.request.ResetPasswordWithTokenRequest;
import com.example.BTL_Mobile.dto.request.VerifyEmailRequest;
import com.example.BTL_Mobile.dto.request.VerifyForgotPasswordCodeRequest;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.AuthResponse;
import com.example.BTL_Mobile.dto.response.PasswordStrengthResponse;
import com.example.BTL_Mobile.dto.response.RegisterTokenResponse;
import com.example.BTL_Mobile.dto.response.ResetPasswordTokenResponse;
import com.example.BTL_Mobile.dto.response.TokenValidationResponse;
import com.example.BTL_Mobile.dto.response.UserResponse;
import com.example.BTL_Mobile.service.AuthService;
import com.example.BTL_Mobile.service.GoogleIdTokenAuthService;
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
  private final GoogleIdTokenAuthService googleIdTokenAuthService;

  /**
   * Bấm "Đăng nhập bằng Google" -> redirect tới trang đăng nhập Google. Project này dành cho mobile
   * app: login xong redirect về deep link elearn://login/callback.
   */
  // @GetMapping("/oauth2/authorize/google")
  // public void authorizeGoogle(HttpServletResponse response) throws IOException {
  //   response.sendRedirect("/oauth2/authorization/google");
  // }

  /**
   * Mobile "popup chọn tài khoản Google" (Google Sign-In / Credential Manager) -> gửi idToken về
   * BE. BE verify idToken với Google và phát JWT + refresh token của hệ thống.
   */
  @PostMapping("/oauth2/google")
  public ResponseEntity<ApiResponse<AuthResponse>> loginGoogleWithIdToken(
      @Valid @RequestBody GoogleIdTokenLoginRequest request
  ) {
    AuthResponse response = googleIdTokenAuthService.loginWithGoogleIdToken(request.getIdToken());
    return ResponseEntity.ok(ApiResponse.success("Đăng nhập Google thành công!", response));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Object>> registerInitiate(
      @Valid @RequestBody RegisterInitiateRequest request) {
    authService.registerInitiate(request);
    return ResponseEntity.ok(ApiResponse.success("OTP has been sent to your email.", null));
  }

  @PostMapping("/register/verify")
  public ResponseEntity<ApiResponse<RegisterTokenResponse>> verifyEmail(
      @Valid @RequestBody VerifyEmailRequest request) {
    RegisterTokenResponse response = authService.verifyEmail(request);
    return ResponseEntity.ok(ApiResponse.success("Verify code successful!", response));
  }

  @PostMapping("/register/complete")
  public ResponseEntity<ApiResponse<AuthResponse>> registerComplete(
      @Valid @RequestBody RegisterCompleteRequest request) {
    AuthResponse response = authService.registerComplete(request);
    return ResponseEntity.ok(ApiResponse.success("Register successful!", response));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", response));
  }

  @PostMapping({"/refresh", "/refresh/"})
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
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
  public ResponseEntity<ApiResponse<Object>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.ok(ApiResponse.success("OTP has been sent to your email.", null));
  }

  @PostMapping("/forgot-password/verify")
  public ResponseEntity<ApiResponse<ResetPasswordTokenResponse>> verifyForgotPasswordCode(
      @Valid @RequestBody VerifyForgotPasswordCodeRequest request
  ) {
    ResetPasswordTokenResponse response = authService.verifyForgotPasswordCode(request);
    return ResponseEntity.ok(ApiResponse.success("Verify code successful!", response));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Object>> resetPassword(
      @Valid @RequestBody ResetPasswordWithTokenRequest request) {
    authService.resetPasswordWithToken(request);
    return ResponseEntity.ok(ApiResponse.success("Reset password successful!", null));
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse<Object>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(request);
    return ResponseEntity.ok(ApiResponse.success("Change password successful!", null));
  }

  // @PostMapping("/password-strength")
  // public ResponseEntity<ApiResponse<PasswordStrengthResponse>> validatePasswordStrength(
  //     @Valid @RequestBody PasswordStrengthRequest request) {
  //   PasswordStrengthResponse response = authService.validatePasswordStrength(request.getPassword());
  //   return ResponseEntity.ok(ApiResponse.success("Validate password successful!", response));
  // }

  @PostMapping("/validate")
  public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
      @RequestHeader("Authorization") String authHeader) {
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
