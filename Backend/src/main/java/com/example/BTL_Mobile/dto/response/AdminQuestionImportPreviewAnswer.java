package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuestionImportPreviewAnswer {
    private String content;
    private boolean correct;
}
