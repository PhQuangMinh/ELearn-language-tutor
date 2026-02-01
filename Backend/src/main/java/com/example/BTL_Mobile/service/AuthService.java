package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.AuthResponse;
import com.example.BTL_Mobile.dto.LoginRequest;
import com.example.BTL_Mobile.dto.RegisterRequest;
import com.example.BTL_Mobile.dto.TokenValidationResponse;
import com.example.BTL_Mobile.dto.UserResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Role;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username đã được sử dụng!", "USERNAME_EXISTS");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được sử dụng!", "EMAIL_EXISTS");
        }

        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .provider("local")
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

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

    public AuthResponse login(LoginRequest request) {
        // Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo token
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new RuntimeException("Authentication principal is not a User instance");
        }
        User user = (User) principal;
        
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
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .build();
    }
}
