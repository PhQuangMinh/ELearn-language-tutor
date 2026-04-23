package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.AdminImportResult;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportCommitResponse;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportPreviewItem;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportPreviewResponse;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import java.util.List;
import com.example.BTL_Mobile.service.AdminExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminImportController {

    private final AdminExcelImportService adminExcelImportService;

    @PostMapping(value = "/words", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminImportResult>> importWords(
            @RequestParam("topicId") Integer topicId,
            @RequestPart("file") MultipartFile file
    ) {
        AdminImportResult result = adminExcelImportService.importWords(topicId, file);
        return ResponseEntity.ok(ApiResponse.success("Import vocabulary từ Excel hoàn tất", result));
    }

    @PostMapping(value = "/flashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminImportResult>> importFlashCards(
            @RequestParam("topicId") Integer topicId,
            @RequestPart("file") MultipartFile file
    ) {
        AdminImportResult result = adminExcelImportService.importFlashCards(topicId, file);
        return ResponseEntity.ok(ApiResponse.success("Import flashcard từ Excel hoàn tất", result));
    }

    @PostMapping(value = "/questions/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminQuestionImportPreviewResponse>> previewQuestions(
            @RequestPart("file") MultipartFile file
    ) {
        AdminQuestionImportPreviewResponse result = adminExcelImportService.previewQuestions(file);
        return ResponseEntity.ok(ApiResponse.success("Preview import question từ Excel thành công", result));
    }

    @PostMapping("/questions/commit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminQuestionImportCommitResponse>> commitQuestions(
            @RequestBody List<AdminQuestionImportPreviewItem> requests
    ) {
        AdminQuestionImportCommitResponse result = adminExcelImportService.commitQuestions(requests);
        return ResponseEntity.ok(ApiResponse.success("Lưu danh sách question preview thành công", result));
    }
}
