package com.example.BTL_Mobile.service.facade;

import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import com.example.BTL_Mobile.dto.response.lesson.QuestionDetailDTO;

import java.util.List;

public interface LessonFacadeService {

    List<QuestionDetailDTO> getQuestions(int lessonId);

    void submit(int lessonId, SubmitLessonRequest submitLesson);
}
