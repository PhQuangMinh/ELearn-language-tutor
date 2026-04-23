package com.example.BTL_Mobile.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuestionImportCommitResponse {
    @Builder.Default
    private int importedCount = 0;

    @Builder.Default
    private int skippedCount = 0;

    @Builder.Default
    private int errorCount = 0;

    @Builder.Default
    private List<Integer> skippedLessonIds = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
