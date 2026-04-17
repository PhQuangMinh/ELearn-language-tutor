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
@RequestMapping("/api/admin/topics/{topicId}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class AdminTopicContentController {

    private final AdminLessonVocabularyService adminLessonVocabularyService;
    private final AdminLessonFlashCardService adminLessonFlashCardService;

    @GetMapping("/words")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminWordResponse>>> listWords(@PathVariable Integer topicId) {
        List<AdminWordResponse> words = adminLessonVocabularyService.listWords(topicId);
        return ResponseEntity.ok(ApiResponse.success("Lấy vocabulary theo topic thành công!", words));
    }

    @PostMapping("/words")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminWordResponse>> addWord(
            @PathVariable Integer topicId,
            @Valid @RequestBody AdminWordCreateRequest request
    ) {
        AdminWordResponse created = adminLessonVocabularyService.addWord(topicId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm word vào topic thành công!", created));
    }

    @PutMapping("/words/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminWordResponse>> updateWord(
            @PathVariable Integer topicId,
            @PathVariable Integer wordId,
            @Valid @RequestBody AdminWordCreateRequest request
    ) {
        AdminWordResponse updated = adminLessonVocabularyService.updateWord(topicId, wordId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật word thành công!", updated));
    }

    @DeleteMapping("/words/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable Integer topicId,
            @PathVariable Integer wordId
    ) {
        adminLessonVocabularyService.removeWordFromTopic(topicId, wordId);
        return ResponseEntity.ok(ApiResponse.success("Xóa word khỏi topic thành công!", null));
    }

    @GetMapping("/flashcards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FlashCardResponse>>> listFlashCards(@PathVariable Integer topicId) {
        List<FlashCardResponse> cards = adminLessonFlashCardService.listFlashCards(topicId);
        return ResponseEntity.ok(ApiResponse.success("Lấy flashcard theo topic thành công!", cards));
    }

    @PostMapping("/flashcards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashCardResponse>> addFlashCard(
            @PathVariable Integer topicId,
            @Valid @RequestBody AdminFlashCardCreateRequest request
    ) {
        FlashCardResponse created = adminLessonFlashCardService.addFlashCard(topicId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo flashcard theo topic thành công!", created));
    }

    /**
     * Xóa toàn bộ flashcard của topic rồi tạo lại flashcard khớp 1-1 với từ vựng hiện có trong topic.
     */
    @PostMapping("/flashcards/sync-from-words")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FlashCardResponse>>> syncFlashCardsFromWords(@PathVariable Integer topicId) {
        List<FlashCardResponse> synced = adminLessonFlashCardService.syncFlashCardsFromTopicWords(topicId);
        return ResponseEntity.ok(ApiResponse.success("Đã đồng bộ flashcard theo từ vựng topic!", synced));
    }

    @PutMapping("/flashcards/{flashCardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashCardResponse>> updateFlashCard(
            @PathVariable Integer topicId,
            @PathVariable Integer flashCardId,
            @Valid @RequestBody AdminFlashCardUpdateRequest request
    ) {
        FlashCardResponse updated = adminLessonFlashCardService.updateFlashCard(topicId, flashCardId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật flashcard thành công!", updated));
    }

    @DeleteMapping("/flashcards/{flashCardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFlashCard(
            @PathVariable Integer topicId,
            @PathVariable Integer flashCardId
    ) {
        adminLessonFlashCardService.deleteFlashCard(topicId, flashCardId);
        return ResponseEntity.ok(ApiResponse.success("Xóa flashcard thành công!", null));
    }
}
