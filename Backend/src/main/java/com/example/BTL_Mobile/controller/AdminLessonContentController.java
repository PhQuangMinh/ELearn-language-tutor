package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.AdminFlashCardCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminFlashCardUpdateRequest;
import com.example.BTL_Mobile.dto.request.AdminWordCreateRequest;
import com.example.BTL_Mobile.dto.response.AdminWordResponse;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.FlashCardResponse;
import com.example.BTL_Mobile.service.AdminLessonFlashCardService;
import com.example.BTL_Mobile.service.AdminLessonVocabularyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/lessons/{lessonId}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminLessonContentController {

    private final AdminLessonVocabularyService adminLessonVocabularyService;
    private final AdminLessonFlashCardService adminLessonFlashCardService;

    @GetMapping("/words")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminWordResponse>>> listWords(@PathVariable Integer lessonId) {
        List<AdminWordResponse> words = adminLessonVocabularyService.listWords(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lấy vocabulary theo lesson thành công!", words));
    }

    @PostMapping("/words")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminWordResponse>> addWord(
            @PathVariable Integer lessonId,
            @Valid @RequestBody AdminWordCreateRequest request
    ) {
        AdminWordResponse created = adminLessonVocabularyService.addWord(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm word vào lesson thành công!", created));
    }

    @PutMapping("/words/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminWordResponse>> updateWord(
            @PathVariable Integer lessonId,
            @PathVariable Integer wordId,
            @Valid @RequestBody AdminWordCreateRequest request
    ) {
        AdminWordResponse updated = adminLessonVocabularyService.updateWord(lessonId, wordId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật word thành công!", updated));
    }

    @DeleteMapping("/words/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable Integer lessonId,
            @PathVariable Integer wordId
    ) {
        adminLessonVocabularyService.removeWordFromLesson(lessonId, wordId);
        return ResponseEntity.ok(ApiResponse.success("Xóa word khỏi lesson thành công!", null));
    }

    @GetMapping("/flashcards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FlashCardResponse>>> listFlashCards(@PathVariable Integer lessonId) {
        List<FlashCardResponse> cards = adminLessonFlashCardService.listFlashCards(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lấy flashcard theo lesson thành công!", cards));
    }

    @PostMapping("/flashcards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashCardResponse>> addFlashCard(
            @PathVariable Integer lessonId,
            @Valid @RequestBody AdminFlashCardCreateRequest request
    ) {
        FlashCardResponse created = adminLessonFlashCardService.addFlashCard(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo flashcard theo lesson thành công!", created));
    }

    @PutMapping("/flashcards/{flashCardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashCardResponse>> updateFlashCard(
            @PathVariable Integer lessonId,
            @PathVariable Integer flashCardId,
            @Valid @RequestBody AdminFlashCardUpdateRequest request
    ) {
        FlashCardResponse updated = adminLessonFlashCardService.updateFlashCard(lessonId, flashCardId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật flashcard thành công!", updated));
    }

    @DeleteMapping("/flashcards/{flashCardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFlashCard(
            @PathVariable Integer lessonId,
            @PathVariable Integer flashCardId
    ) {
        adminLessonFlashCardService.deleteFlashCard(lessonId, flashCardId);
        return ResponseEntity.ok(ApiResponse.success("Xóa flashcard thành công!", null));
    }
}

