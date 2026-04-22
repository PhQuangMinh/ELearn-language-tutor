package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.DeviceTokenRequest;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.model.UserDeviceToken;
import com.example.BTL_Mobile.repository.UserDeviceTokenRepository;
import com.example.BTL_Mobile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerToken(Integer userId, DeviceTokenRequest request) {
        String token = request.getToken().trim();
        Optional<UserDeviceToken> existing = userDeviceTokenRepository.findByFcmToken(token);

        UserDeviceToken entity = existing.orElseGet(UserDeviceToken::new);
        User userRef = userRepository.getReferenceById(userId);

        entity.setUser(userRef);
        entity.setFcmToken(token);
        entity.setPlatform(normalizePlatform(request.getPlatform()));
        entity.setDeviceId(trimToNull(request.getDeviceId()));
        entity.setAppVersion(trimToNull(request.getAppVersion()));
        entity.setActive(true);
        entity.setLastSeenAt(LocalDateTime.now());

        userDeviceTokenRepository.save(entity);
    }

    @Transactional
    public void unregisterToken(Integer userId, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        userDeviceTokenRepository.findByFcmTokenAndUserId(token.trim(), userId)
                .ifPresent(entity -> {
                    entity.setActive(false);
                    entity.setLastSeenAt(LocalDateTime.now());
                    userDeviceTokenRepository.save(entity);
                });
    }

    @Transactional
    public void deactivateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        userDeviceTokenRepository.findByFcmToken(token.trim())
                .ifPresent(entity -> {
                    entity.setActive(false);
                    userDeviceTokenRepository.save(entity);
                });
    }

    @Transactional(readOnly = true)
    public List<UserDeviceToken> findReminderTargets(LocalDateTime startOfDay) {
        return userDeviceTokenRepository.findReminderTargets(startOfDay);
    }

    private String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "ANDROID";
        }
        return platform.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
