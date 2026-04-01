package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.CloudinaryUploadResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final RestClient restClient = RestClient.create();

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.folder:elearn/avatars}")
    private String folder;

    public String uploadAvatar(MultipartFile avatarFile, Integer userId) {
        CloudinaryUploadResponse response = uploadImage(
                avatarFile,
                folder,
                "user_" + userId
        );
        return response.getSecureUrl();
    }

    public CloudinaryUploadResponse uploadImage(MultipartFile imageFile, String folderPath, String publicIdPrefix) {
        validateImageFile(imageFile);

        if (cloudName == null || cloudName.isBlank()) {
            throw new BusinessException("Chưa cấu hình cloudinary.cloud-name", "CLOUDINARY_NOT_CONFIGURED");
        }
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new BusinessException("Chưa cấu hình cloudinary.api-key hoặc cloudinary.api-secret", "CLOUDINARY_NOT_CONFIGURED");
        }

        try {
            byte[] content = imageFile.getBytes();
            long timestamp = System.currentTimeMillis() / 1000;
            String effectiveFolder = (folderPath == null || folderPath.isBlank()) ? folder : folderPath;
            String safePrefix = (publicIdPrefix == null || publicIdPrefix.isBlank())
                    ? "asset"
                    : publicIdPrefix;
            String publicId = safePrefix + "_" + UUID.randomUUID();
            String signature = buildSignature(effectiveFolder, publicId, timestamp);

            ByteArrayResource fileResource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename() == null
                        ? "avatar-" + UUID.randomUUID() + ".jpg"
                        : imageFile.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("folder", effectiveFolder);
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

            return CloudinaryUploadResponse.builder()
                    .secureUrl(response.get("secure_url").toString())
                    .publicId(valueAsString(response.get("public_id")))
                    .format(valueAsString(response.get("format")))
                    .width(valueAsInteger(response.get("width")))
                    .height(valueAsInteger(response.get("height")))
                    .bytes(valueAsLong(response.get("bytes")))
                    .originalFilename(imageFile.getOriginalFilename())
                    .build();
        } catch (IOException e) {
            throw new BusinessException("Không thể đọc file ảnh", "INVALID_AVATAR_FILE");
        } catch (Exception e) {
            throw new BusinessException("Upload ảnh thất bại: " + e.getMessage(), "CLOUDINARY_UPLOAD_FAILED");
        }
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException("Avatar không được để trống", "AVATAR_REQUIRED");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("File avatar phải là ảnh", "INVALID_AVATAR_TYPE");
        }
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer valueAsInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Long valueAsLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
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
