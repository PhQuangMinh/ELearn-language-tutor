package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.exception.BusinessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final RestClient restClient = RestClient.builder().build();

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.folder:elearn/avatars}")
    private String folder;

    public String uploadAvatar(MultipartFile avatarFile, Integer userId) {
        validateImageFile(avatarFile);

        if (cloudName == null || cloudName.isBlank()) {
            throw new BusinessException("Chưa cấu hình cloudinary.cloud-name", "CLOUDINARY_NOT_CONFIGURED");
        }
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new BusinessException("Chưa cấu hình cloudinary.api-key hoặc cloudinary.api-secret", "CLOUDINARY_NOT_CONFIGURED");
        }

        try {
            byte[] content = avatarFile.getBytes();
            long timestamp = System.currentTimeMillis() / 1000;
            String publicId = "user_" + userId + "_" + UUID.randomUUID();
            String signature = buildSignature(folder, publicId, timestamp);

            ByteArrayResource fileResource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return avatarFile.getOriginalFilename() == null
                        ? "avatar-" + UUID.randomUUID() + ".jpg"
                        : avatarFile.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("folder", folder);
            body.add("public_id", publicId);
            body.add("timestamp", String.valueOf(timestamp));
            body.add("api_key", apiKey);
            body.add("signature", signature);

            Map<?, ?> response = restClient.post()
                .uri("https://api.cloudinary.com/v1_1/{cloudName}/image/upload", cloudName)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

            if (response == null || response.get("secure_url") == null) {
                throw new BusinessException("Upload ảnh thất bại từ Cloudinary", "CLOUDINARY_UPLOAD_FAILED");
            }

            return response.get("secure_url").toString();
        } catch (IOException e) {
            throw new BusinessException("Không thể đọc file ảnh", "INVALID_AVATAR_FILE");
        } catch (Exception e) {
            throw new BusinessException("Upload ảnh thất bại: " + e.getMessage(), "CLOUDINARY_UPLOAD_FAILED");
        }
    }

    private void validateImageFile(MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new BusinessException("Avatar không được để trống", "AVATAR_REQUIRED");
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("File avatar phải là ảnh", "INVALID_AVATAR_TYPE");
        }
    }

    private String buildSignature(String folder, String publicId, long timestamp) {
        String paramsToSign = "folder=" + folder + "&public_id=" + publicId + "&timestamp=" + timestamp;
        String payload = paramsToSign + apiSecret;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("Không thể tạo chữ ký Cloudinary", "CLOUDINARY_SIGNATURE_FAILED");
        }
    }
}
