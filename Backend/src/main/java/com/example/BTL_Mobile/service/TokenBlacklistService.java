package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.model.BlacklistedToken;
import com.example.BTL_Mobile.repository.BlacklistedTokenRepository;
import com.example.BTL_Mobile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Kiểm tra xem token có bị blacklist không
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.findByToken(token)
                .map(blacklistedToken -> {
                    // Nếu token đã hết hạn, xóa khỏi blacklist
                    if (blacklistedToken.isExpired()) {
                        blacklistedTokenRepository.delete(blacklistedToken);
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Thêm token vào blacklist
     */
    @Transactional
    public void blacklistToken(String token) {
        // Kiểm tra xem token đã có trong blacklist chưa
        if (blacklistedTokenRepository.findByToken(token).isPresent()) {
            return; // Đã có rồi, không cần thêm
        }

        try {
            // Lấy expiration time từ token
            Date expirationDate = jwtTokenProvider.extractExpiration(token);
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    expirationDate.toInstant(),
                    java.time.ZoneId.systemDefault()
            );

            // Tạo blacklisted token
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();

            blacklistedTokenRepository.save(blacklistedToken);
        } catch (Exception e) {
            // Nếu không parse được token (token không hợp lệ), không cần blacklist
            // Vì token không hợp lệ sẽ bị reject ở filter rồi
        }
    }

    /**
     * Xóa các token đã hết hạn khỏi blacklist (cleanup)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteExpiredTokens();
    }
}
