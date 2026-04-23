package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.ForgotPasswordRequest;
import com.example.BTL_Mobile.dto.request.LoginRequest;
import com.example.BTL_Mobile.dto.request.ChangePasswordRequest;
import com.example.BTL_Mobile.dto.request.RegisterCompleteRequest;
import com.example.BTL_Mobile.dto.request.RegisterInitiateRequest;
import com.example.BTL_Mobile.dto.request.ResetPasswordWithTokenRequest;
import com.example.BTL_Mobile.dto.request.VerifyEmailRequest;
import com.example.BTL_Mobile.dto.request.VerifyForgotPasswordCodeRequest;
import com.example.BTL_Mobile.dto.response.AuthResponse;
import com.example.BTL_Mobile.dto.response.PasswordStrengthResponse;
import com.example.BTL_Mobile.dto.response.RegisterTokenResponse;
import com.example.BTL_Mobile.dto.response.ResetPasswordTokenResponse;
import com.example.BTL_Mobile.dto.response.TokenValidationResponse;
import com.example.BTL_Mobile.dto.response.UserResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.enums.ERole;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.RefreshTokenRepository;
import com.example.BTL_Mobile.repository.UserRepository;
import com.example.BTL_Mobile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.security.SecureRandom;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;

    @Transactional
    public void registerInitiate(RegisterInitiateRequest request) {
        // If email is already used by an active account => block
        User existing = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existing != null && existing.isEnabled()) {
            throw new BusinessException("This email has been used. Please choose another email!", "EMAIL_EXISTS");
        }

        // Create or reuse a pending (disabled) user to allow resend OTP
        User user = existing;
        if (user == null) {
            String username = generateUsernameFromEmail(request.getEmail());
            user = User.builder()
                    .username(username)
                    .email(request.getEmail())
                    // placeholder password; will be overwritten in complete step
                    .password(passwordEncoder.encode(generateResetToken()))
                    .fullName(request.getFullName())
                    .provider("local")
                    .role(ERole.USER)
                    .enabled(false)
                    .build();
        } else {
            // Update name on re-initiate
            user.setFullName(request.getFullName());
        }

        // Sinh mã OTP 6 chữ số và hạn 10 phút
        String otp = generateOtp();
        user.setEmailVerificationCode(otp);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // Gửi email xác thực
        emailService.sendOtp(user.getEmail(), "Verify your registration", otp);
    }

    @Transactional
    public RegisterTokenResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No account associated with this email.", "USER_NOT_FOUND"));

        if (user.isEnabled()) {
            throw new BusinessException("Account is already verified.", "ALREADY_VERIFIED");
        }

        if (user.getEmailVerificationCode() == null ||
                user.getEmailVerificationExpiry() == null ||
                !user.getEmailVerificationCode().equals(request.getCode())) {
            throw new BusinessException("Your verification code is incorrect.", "INVALID_VERIFICATION_CODE");
        }

        if (LocalDateTime.now().isAfter(user.getEmailVerificationExpiry())) {
            throw new BusinessException("Your verification code is incorrect.", "VERIFICATION_CODE_EXPIRED");
        }

        // OTP verified; DO NOT enable account yet (password step comes after)
        user.setEmailVerificationCode(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        // issue short-lived registerToken for password completion
        String registerToken = jwtTokenProvider.generateRegisterToken(user.getEmail(), 10 * 60 * 1000L);
        return new RegisterTokenResponse(registerToken);
    }

    @Transactional
    public AuthResponse registerComplete(RegisterCompleteRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No account associated with this email.", "USER_NOT_FOUND"));

        if (user.isEnabled()) {
            throw new BusinessException("This email has been used. Please choose another email!", "EMAIL_EXISTS");
        }

        if (!jwtTokenProvider.validateRegisterToken(request.getRegisterToken(), request.getEmail())) {
            throw new BusinessException("Your verification code is incorrect.", "INVALID_REGISTER_TOKEN");
        }

        validatePasswordPolicy(request.getPassword());

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Tìm user theo email được gửi từ client
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        "Email hoặc mật khẩu không đúng.",
                        "INVALID_CREDENTIALS"
                ));

        // Xác thực người dùng bằng username nội bộ (Spring Security vẫn load bằng username)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo token
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new RuntimeException("Authentication principal is not a User instance");
        }
        user = (User) principal;
        if (!user.isEnabled()) {
            throw new BusinessException("Email is not verified.", "EMAIL_NOT_VERIFIED");
        }
        
        // Tạo access token và refresh token
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        // Verify refresh token
        com.example.BTL_Mobile.model.RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenString);
        User user = refreshToken.getUser();

        // Revoke refresh token cũ (best practice: revoke trước khi tạo mới)
        refreshTokenService.revokeRefreshToken(refreshTokenString);

        // Tạo access token mới
        String newAccessToken = jwtTokenProvider.generateToken(user);

        // Tạo refresh token mới (rotate refresh token)
        String newRefreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    @Transactional
    public void logout(String refreshTokenString, String accessToken) {
        // Blacklist access token nếu có (revoke ngay lập tức)
        if (accessToken != null && jwtTokenProvider.isValidToken(accessToken)) {
            tokenBlacklistService.blacklistToken(accessToken);
        }
        
        // Tìm refresh token (nếu tồn tại)
        com.example.BTL_Mobile.model.RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenString)
                .orElse(null);
        
        if (refreshToken != null) {
            // Nếu token tồn tại, revoke TẤT CẢ refresh tokens của user đó (logout tất cả devices)
            User user = refreshToken.getUser();
            user.setLastLogoutAt(LocalDateTime.now());
            userRepository.save(user);
            refreshTokenService.revokeAllUserTokens(user);
        }
        // Nếu token không tồn tại (đã bị xóa sau khi refresh), cũng OK (idempotent)
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            // Token bị vô hiệu nếu user đã logout sau khi token được cấp
            String usernameForLogoutCheck = jwtTokenProvider.extractUsername(token);
            UserDetails userDetailsForLogoutCheck = userDetailsService.loadUserByUsername(usernameForLogoutCheck);
            if (userDetailsForLogoutCheck instanceof User user) {
                LocalDateTime lastLogoutAt = user.getLastLogoutAt();
                if (lastLogoutAt != null) {
                    Date issuedAt = jwtTokenProvider.extractIssuedAt(token);
                    if (issuedAt != null) {
                        boolean issuedBeforeLogout = issuedAt.toInstant().isBefore(
                                lastLogoutAt.atZone(ZoneId.systemDefault()).toInstant()
                        );
                        if (issuedBeforeLogout) {
                            return TokenValidationResponse.builder()
                                    .valid(false)
                                    .message("Token đã bị thu hồi (đã logout)")
                                    .build();
                        }
                    }
                }
            }

            // Kiểm tra token có bị blacklist không (đã logout)
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token đã bị thu hồi (đã logout)")
                        .build();
            }
            
            if (!jwtTokenProvider.isValidToken(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token không hợp lệ hoặc đã hết hạn")
                        .build();
            }

            String username = jwtTokenProvider.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token, userDetails)) {
                return TokenValidationResponse.builder()
                        .valid(true)
                        .username(username)
                        .message("Token hợp lệ")
                        .build();
            } else {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token không hợp lệ")
                        .build();
            }
        } catch (Exception e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token không hợp lệ: " + e.getMessage())
                    .build();
        }
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new BusinessException("Người dùng chưa đăng nhập!", "UNAUTHORIZED");
        }

        User user = (User) authentication.getPrincipal();
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No account associated with this email.", "EMAIL_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new BusinessException("Email is not verified.", "EMAIL_NOT_VERIFIED");
        }

        String otp = generateOtp();
        user.setResetPasswordCode(otp);
        user.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));
        // clear any previous reset token
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        emailService.sendOtp(user.getEmail(), "Mã đặt lại mật khẩu", otp);
    }

    @Transactional
    public ResetPasswordTokenResponse verifyForgotPasswordCode(VerifyForgotPasswordCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No account associated with this email.", "USER_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new BusinessException("Email is not verified.", "EMAIL_NOT_VERIFIED");
        }

        if (user.getResetPasswordCode() == null ||
                user.getResetPasswordExpiry() == null ||
                !user.getResetPasswordCode().equals(request.getCode())) {
            throw new BusinessException("Your verification code is incorrect.", "INVALID_RESET_CODE");
        }

        if (LocalDateTime.now().isAfter(user.getResetPasswordExpiry())) {
            throw new BusinessException("Your verification code is incorrect.", "RESET_CODE_EXPIRED");
        }

        // Mark code as consumed, issue short-lived reset token
        user.setResetPasswordCode(null);
        user.setResetPasswordExpiry(null);
        String resetToken = generateResetToken();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        return new ResetPasswordTokenResponse(resetToken);
    }

    @Transactional
    public void resetPasswordWithToken(ResetPasswordWithTokenRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("No account associated with this email.", "USER_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new BusinessException("Email is not verified.", "EMAIL_NOT_VERIFIED");
        }

        if (request.getConfirmPassword() == null || !request.getConfirmPassword().equals(request.getNewPassword())) {
            throw new BusinessException("Confirm password does not match!", "CONFIRM_PASSWORD_MISMATCH");
        }

        validatePasswordPolicy(request.getNewPassword());

        if (user.getResetPasswordToken() == null ||
                user.getResetPasswordTokenExpiry() == null ||
                !user.getResetPasswordToken().equals(request.getResetToken())) {
            throw new BusinessException("Your verification code is incorrect.", "INVALID_RESET_TOKEN");
        }

        if (LocalDateTime.now().isAfter(user.getResetPasswordTokenExpiry())) {
            throw new BusinessException("Your verification code is incorrect.", "RESET_TOKEN_EXPIRED");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        user.setLastLogoutAt(LocalDateTime.now());
        userRepository.save(user);

        // Revoke toàn bộ refresh tokens hiện tại
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new BusinessException("Người dùng chưa đăng nhập!", "UNAUTHORIZED");
        }

        User user = (User) authentication.getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect!", "INVALID_CURRENT_PASSWORD");
        }

        if (request.getConfirmPassword() == null || !request.getConfirmPassword().equals(request.getNewPassword())) {
            throw new BusinessException("Confirm password does not match!", "CONFIRM_PASSWORD_MISMATCH");
        }

        validatePasswordPolicy(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastLogoutAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(user);
    }

    public PasswordStrengthResponse validatePasswordStrength(String password) {
        return PasswordStrengthEvaluator.evaluate(password);
    }

    private void validatePasswordPolicy(String password) {
        PasswordStrengthResponse result = PasswordStrengthEvaluator.evaluate(password);
        if (!result.isPass()) {
            throw new BusinessException(result.getMessage(), "WEAK_PASSWORD");
        }
    }

    private String generateOtp() {
        int code = (int) (Math.random() * 900000) + 100000; // 6 digits
        return String.valueOf(code);
    }

    private String generateResetToken() {
        // 32 bytes => 64 hex chars
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateUsernameFromEmail(String email) {
        String base = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : "user";
        base = base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        if (base.length() < 4) {
            base = (base + "user").substring(0, Math.min(50, base.length() + 4));
        }

        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            String suffix = "_" + counter;
            int maxBaseLen = Math.max(1, 50 - suffix.length());
            String trimmedBase = base.length() > maxBaseLen ? base.substring(0, maxBaseLen) : base;
            username = trimmedBase + suffix;
            counter++;
        }
        return username;
    }
}
