package com.example.BTL_Mobile.dto.request.lesson;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SubmitLessonRequest {

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private List<SubmitQuestionDTO> questionAnswers;

}
