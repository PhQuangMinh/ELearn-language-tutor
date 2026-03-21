package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.ConversationImproveRequest;
import com.example.BTL_Mobile.dto.response.ConversationImproveResponse;
import com.example.BTL_Mobile.service.ConversationImproveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
@Tag(name = "Conversation", description = "API cải thiện câu hội thoại bằng Gemini")
public class ConversationController {

    private final ConversationImproveService conversationImproveService;

    @PostMapping({"/conversation/improve", "/api/conversation/improve"})
    @Operation(
            summary = "Cải thiện câu theo context",
            description = "Nhận text và context, gọi Gemini để trả về original/improved/explanation"
    )
    public ResponseEntity<ConversationImproveResponse> improve(
            @Valid @RequestBody ConversationImproveRequest request
    ) {
        ConversationImproveResponse data = conversationImproveService.improve(request);
        return ResponseEntity.ok(data);
    }
}
