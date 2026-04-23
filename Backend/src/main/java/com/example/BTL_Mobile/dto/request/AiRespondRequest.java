package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRespondRequest {

    private int speakingSessionId;

    @NotBlank
    private String scenarioDescription;

    @NotBlank
    private String taskDescription;

    @NotBlank
    private String conversationHistory;

    @NotBlank
    private String userMessage;
}

