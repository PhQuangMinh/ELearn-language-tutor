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
public class ConversationImproveRequest {

    @NotBlank
    private String text;

    @NotBlank
    private String context;
}
