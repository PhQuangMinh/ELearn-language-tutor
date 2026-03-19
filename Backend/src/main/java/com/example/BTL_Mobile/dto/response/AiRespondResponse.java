package com.example.BTL_Mobile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRespondResponse {

    @JsonProperty("ai_message")
    private String aiMessage;

    @JsonProperty("ai_message_translation")
    private String aiMessageTranslation;

    @JsonProperty("user_hints")
    private UserHints userHints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserHints {
        @JsonProperty("analysis")
        private String analysis;

        @JsonProperty("suggestion")
        private String suggestion;

        @JsonProperty("example")
        private String example;
    }
}

