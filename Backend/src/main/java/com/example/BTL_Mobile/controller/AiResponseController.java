package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.AiRespondRequest;
import com.example.BTL_Mobile.dto.response.AiRespondResponse;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.service.AiResponseService;
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
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
@Tag(name = "AI", description = "API phản hồi người dùng bằng Gemini")
public class AiResponseController {

    private final AiResponseService aiResponseService;

    @PostMapping("/respond")
    @Operation(summary = "Phản hồi người dùng bằng AI",
            description = "Nhận mô tả cuộc hội thoại, nhiệm vụ và tin nhắn mới, trả về ai_message + translation + user_hints")
    public ResponseEntity<ApiResponse<AiRespondResponse>> respond(
            @Valid @RequestBody AiRespondRequest request
    ) {
        AiRespondResponse data = aiResponseService.respond(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

