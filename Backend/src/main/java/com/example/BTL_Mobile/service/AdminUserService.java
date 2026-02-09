package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.AdminUserCreateRequest;
import com.example.BTL_Mobile.dto.AdminUserUpdateRequest;
import com.example.BTL_Mobile.dto.UserResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.enums.ERole;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    public UserResponse getById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy user", "USER_NOT_FOUND"));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse create(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username đã được sử dụng!", "USERNAME_EXISTS");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được sử dụng!", "EMAIL_EXISTS");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .provider("local")
                .providerId(null)
            .role(request.getRole() == null ? ERole.USER : request.getRole())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse update(Integer id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy user", "USER_NOT_FOUND"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("Username đã được sử dụng!", "USERNAME_EXISTS");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BusinessException("Email đã được sử dụng!", "EMAIL_EXISTS");
                }
            });
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setProvider("local");
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy user", "USER_NOT_FOUND");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() == null ? null : user.getRole().name())
                .build();
    }
}
