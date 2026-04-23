package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.CloudinaryUploadResponse;
import com.example.BTL_Mobile.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminMediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CloudinaryUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder
    ) {
        String effectiveFolder = (folder == null || folder.isBlank()) ? "elearn/admin" : folder;
        CloudinaryUploadResponse upload = cloudinaryService.uploadImage(file, effectiveFolder, "admin_asset");
        return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công!", upload));
    }
}