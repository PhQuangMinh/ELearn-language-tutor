package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.AuthResponse;
import com.example.BTL_Mobile.model.enums.ERole;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.UserRepository;
import com.example.BTL_Mobile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * Dùng cho OAuth2 redirect flow: sau khi Google/FB redirect về, Spring đã đổi code lấy user.
     * Tìm hoặc tạo user trong DB và phát JWT + refresh token.
     */
    @Transactional
    public AuthResponse findOrCreateAndIssueTokens(String providerId, String email, String name, String provider) {
        User user = findOrCreateOAuthUser(providerId, email, name, provider);
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

    private User findOrCreateOAuthUser(String providerId, String email, String name, String provider) {
        // Tìm user theo provider và providerId
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        if (user == null) {
            // Nếu không tìm thấy, tìm theo email (nếu có email)
            if (email != null && !email.isEmpty()) {
                user = userRepository.findByEmail(email).orElse(null);
            }

            if (user != null) {
                // Nếu user đã tồn tại với email này, cập nhật provider info
                user.setProvider(provider);
                user.setProviderId(providerId);
                if (user.getFullName() == null && name != null) {
                    user.setFullName(name);
                }
            } else {
                // Tạo user mới
                String username = generateUsername(email, name, provider, providerId);
                String userEmail = email != null && !email.isEmpty() ? email : providerId + "@" + provider + ".oauth";
                
                user = User.builder()
                        .username(username)
                        .email(userEmail)
                        .password("OAUTH_USER_NO_PASSWORD")  // OAuth users không có password thật, set giá trị mặc định
                        .fullName(name)
                        .provider(provider)
                        .providerId(providerId)
                        .role(ERole.USER)
                        .enabled(true)
                        .build();
            }
            userRepository.save(user);
        }

        return user;
    }

    private String generateUsername(String email, String name, String provider, String providerId) {
        String baseUsername;
        
        if (email != null && !email.isEmpty()) {
            baseUsername = email.split("@")[0];
        } else if (name != null && !name.isEmpty()) {
            baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            baseUsername = provider + "_" + providerId.substring(0, Math.min(8, providerId.length()));
        }
        
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + provider + "_" + counter;
            counter++;
        }

        return username;
    }
}
