package com.example.BTL_Mobile.dto.response;

import com.example.BTL_Mobile.model.enums.EQuestionType;
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
public class AdminQuestionImportPreviewItem {
    private Integer sourceRow;
    private Integer lessonId;
    private String content;
    private EQuestionType type;
    private boolean repeatable;
    private String mediaUrl;

    @Builder.Default
    private List<AdminQuestionImportPreviewAnswer> answers = new ArrayList<>();
}
