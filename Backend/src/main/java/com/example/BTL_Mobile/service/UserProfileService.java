package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.UserProfileResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public UserProfileResponse getCurrentUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        return toUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(Integer userId, String fullName, MultipartFile avatar) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        String normalizedFullName = fullName == null ? "" : fullName.trim();
        if (normalizedFullName.isEmpty()) {
            throw new BusinessException("Your name cannot be empty!", "FULL_NAME_REQUIRED");
        }
        if (normalizedFullName.length() < 4) {
            throw new BusinessException("Your name's length must be >= 4 characters.", "FULL_NAME_TOO_SHORT");
        }
        if (normalizedFullName.length() > 100) {
            throw new BusinessException("Họ tên tối đa 100 ký tự", "FULL_NAME_TOO_LONG");
        }

        user.setFullName(normalizedFullName);

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadAvatar(avatar, user.getId());
            user.setAvatarUrl(avatarUrl);
        }

        log.info("current avatar url: {}", user.getAvatarUrl());

        User saved = userRepository.save(user);
        return toUserProfileResponse(saved);
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
            .fullName(user.getFullName())
            .email(user.getEmail())
            .avatarUrl(user.getAvatarUrl())
            .provider(user.getProvider())
            .build();
    }
}
