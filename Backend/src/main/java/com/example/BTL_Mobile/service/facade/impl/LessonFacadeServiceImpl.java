package com.example.BTL_Mobile.service.facade.impl;

import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import com.example.BTL_Mobile.dto.response.lesson.LessonSubmittedDTO;
import com.example.BTL_Mobile.dto.response.lesson.QuestionDetailDTO;
import com.example.BTL_Mobile.mapper.QuestionMapper;
import com.example.BTL_Mobile.model.*;
import com.example.BTL_Mobile.service.LessonService;
import com.example.BTL_Mobile.service.facade.LessonFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LessonFacadeServiceImpl implements LessonFacadeService {

    private final LessonService lessonService;

    private final QuestionMapper questionMapper;

    @Override
    public List<QuestionDetailDTO> getQuestions(int lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Set<Question> questions = lesson.getQuestions();
        return questions.stream().map(questionMapper::toQuestionDetailDTO)
                .toList();
    }

    @Override
    public LessonSubmittedDTO submit(int lessonId, SubmitLessonRequest submitLesson) {
        return lessonService.submitLesson(lessonId, submitLesson);
    }

}
