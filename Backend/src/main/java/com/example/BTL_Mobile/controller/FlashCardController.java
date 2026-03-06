package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.FlashCardResponse;
import com.example.BTL_Mobile.service.FlashCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    @Operation(summary = "Lấy danh sách flashcard",
            description = "Trả về danh sách flashcard, có thể lọc theo topic")
    public ResponseEntity<ApiResponse<List<FlashCardResponse>>> getFlashCards(
            @RequestParam(required = false) Integer topicId
    ) {
        List<FlashCardResponse> flashCards = flashCardService.getFlashCards(topicId);
        return ResponseEntity.ok(ApiResponse.success(flashCards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 flashcard")
    public ResponseEntity<ApiResponse<FlashCardResponse>> getFlashCardById(@PathVariable Integer id) {
        FlashCardResponse flashCard = flashCardService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(flashCard));
    }
}

