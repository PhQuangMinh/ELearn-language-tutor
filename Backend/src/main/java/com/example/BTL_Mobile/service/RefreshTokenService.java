package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.RefreshToken;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.RefreshTokenRepository;
import com.example.BTL_Mobile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Xóa refresh token cũ của user nếu có
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // Tạo refresh token mới
        String token = jwtTokenProvider.generateRefreshToken(user);
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token không tồn tại!", "REFRESH_TOKEN_NOT_FOUND"));

        if (!refreshToken.isValid()) {
            throw new BusinessException("Refresh token đã hết hạn hoặc đã bị thu hồi!", "REFRESH_TOKEN_INVALID");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
        // Nếu token không tồn tại, không throw exception (idempotent)
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}
