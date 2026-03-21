package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.request.ConversationImproveRequest;
import com.example.BTL_Mobile.dto.response.ConversationImproveResponse;
import com.example.BTL_Mobile.service.ConversationImproveService;
import com.example.BTL_Mobile.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationImproveServiceImpl implements ConversationImproveService {

    private final GeminiService geminiService;

    @Override
    public ConversationImproveResponse improve(ConversationImproveRequest request) {
        String prompt = buildPrompt(request);
        ConversationImproveResponse response =
                geminiService.callGemini(prompt, ConversationImproveResponse.class);

        if (response == null) {
            throw new RuntimeException("AI không trả về dữ liệu hợp lệ");
        }

        if (response.getOriginal() == null || response.getOriginal().isBlank()) {
            response.setOriginal(request.getText());
        }

        if (response.getImproved() == null || response.getImproved().isBlank()
                || response.getExplanation() == null || response.getExplanation().isBlank()) {
            throw new RuntimeException("AI trả về thiếu trường improved hoặc explanation");
        }

        return response;
    }

    private String buildPrompt(ConversationImproveRequest request) {
        return String.format("""
                You are an English conversation improvement assistant.
                Based on the user's text and context, rewrite the text to sound more natural and suitable.

                Return STRICT JSON only with exactly 3 fields:
                {
                  "original": "...",
                  "improved": "...",
                  "explanation": "..."
                }

                Rules:
                - Keep "original" exactly the same as user input text.
                - "improved" must be an improved English sentence.
                - "explanation" must be in Vietnamese, concise and practical.
                - Do not include markdown, code block, or any extra keys/text.

                user_text: %s
                context: %s
                """, request.getText(), request.getContext());
    }
}
