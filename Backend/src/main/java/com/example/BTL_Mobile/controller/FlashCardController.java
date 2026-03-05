package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.FlashCardResponse;
import com.example.BTL_Mobile.service.FlashCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
@Tag(name = "Flashcards", description = "API flashcard từ vựng")
public class FlashCardController {

    private final FlashCardService flashCardService;

    @GetMapping
    @Operation(summary = "Lấy danh sách flashcard (phân trang)",
            description = "Trả về danh sách flashcard với từ, phiên âm, nghĩa, ví dụ và ảnh minh hoạ, hỗ trợ phân trang")
    public ResponseEntity<ApiResponse<Page<FlashCardResponse>>> getFlashCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<FlashCardResponse> flashCards = flashCardService.getFlashCards(pageable);
        return ResponseEntity.ok(ApiResponse.success(flashCards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 flashcard")
    public ResponseEntity<ApiResponse<FlashCardResponse>> getFlashCardById(@PathVariable Integer id) {
        FlashCardResponse flashCard = flashCardService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(flashCard));
    }
}

