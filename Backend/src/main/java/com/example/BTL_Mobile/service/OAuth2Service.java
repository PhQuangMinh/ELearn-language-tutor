package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.AuthResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Role;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.UserRepository;
import com.example.BTL_Mobile.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth2.facebook.app-id:}")
    private String facebookAppId;

    @Value("${oauth2.facebook.app-secret:}")
    private String facebookAppSecret;

    @Transactional
    public AuthResponse loginWithGoogle(String accessToken) {
        // Verify token với Google API
        GoogleUserInfo googleUser = verifyGoogleToken(accessToken);
        
        // Tìm hoặc tạo user
        User user = findOrCreateOAuthUser(
                googleUser.getId(),
                googleUser.getEmail(),
                googleUser.getName(),
                "google"
        );

        // Tạo JWT tokens
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
    public AuthResponse loginWithFacebook(String accessToken) {
        // Verify token với Facebook API
        FacebookUserInfo facebookUser = verifyFacebookToken(accessToken);
        
        // Tìm hoặc tạo user
        User user = findOrCreateOAuthUser(
                facebookUser.getId(),
                facebookUser.getEmail(),
                facebookUser.getName(),
                "facebook"
        );

        // Tạo JWT tokens
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

    private GoogleUserInfo verifyGoogleToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) response.getBody();
                return new GoogleUserInfo(
                        (String) userInfo.get("id"),
                        (String) userInfo.get("email"),
                        (String) userInfo.get("name")
                );
            }
            throw new BusinessException("Không thể xác thực token Google", "GOOGLE_TOKEN_INVALID");
        } catch (Exception e) {
            throw new BusinessException("Token Google không hợp lệ: " + e.getMessage(), "GOOGLE_TOKEN_INVALID");
        }
    }

    private FacebookUserInfo verifyFacebookToken(String accessToken) {
        try {
            // Verify token với Facebook API
            // Request fields: id, name, email (email có thể null nếu user không cấp quyền)
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            
            // Nếu có app_id và app_secret, có thể verify token trước
            if (facebookAppId != null && !facebookAppId.isEmpty() && 
                facebookAppSecret != null && !facebookAppSecret.isEmpty()) {
                // Verify token với app credentials (optional - để đảm bảo token thuộc về app của bạn)
                String debugUrl = String.format(
                    "https://graph.facebook.com/debug_token?input_token=%s&access_token=%s|%s",
                    accessToken, facebookAppId, facebookAppSecret
                );
                try {
                    @SuppressWarnings("rawtypes")
                    ResponseEntity<Map> debugResponse = restTemplate.getForEntity(debugUrl, Map.class);
                    if (debugResponse.getStatusCode().is2xxSuccessful() && debugResponse.getBody() != null) {
                        Object dataObj = debugResponse.getBody().get("data");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> debugData = (dataObj instanceof Map<?, ?>) ? (Map<String, Object>) dataObj : null;
                        if (debugData != null && !Boolean.TRUE.equals(debugData.get("is_valid"))) {
                            throw new BusinessException("Token Facebook không hợp lệ hoặc không thuộc về app này", "FACEBOOK_TOKEN_INVALID");
                        }
                    }
                } catch (Exception e) {
                    // Nếu verify fail, vẫn tiếp tục với cách verify cũ
                    // Log warning nhưng không throw error
                }
            }
            
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) response.getBody();
                String email = (String) userInfo.get("email");
                // Facebook có thể không trả về email nếu user không cấp quyền
                if (email == null || email.isEmpty()) {
                    email = null;  // Cho phép null email
                }
                return new FacebookUserInfo(
                        (String) userInfo.get("id"),
                        email,
                        (String) userInfo.get("name")
                );
            }
            throw new BusinessException("Không thể xác thực token Facebook", "FACEBOOK_TOKEN_INVALID");
        } catch (Exception e) {
            throw new BusinessException("Token Facebook không hợp lệ: " + e.getMessage(), "FACEBOOK_TOKEN_INVALID");
        }
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
                        .role(Role.USER)
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

    // Inner classes để parse response
    private static class GoogleUserInfo {
        private final String id;
        private final String email;
        private final String name;

        public GoogleUserInfo(String id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }

    private static class FacebookUserInfo {
        private final String id;
        private final String email;
        private final String name;

        public FacebookUserInfo(String id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }
}
