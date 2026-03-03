package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import java.util.List;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.dto.response.LessonInTopicResponse;

import java.util.Optional;

public interface LessonService {

    Optional<Lesson> getLessonById(int id);
    List<LessonInTopicResponse> getLessonsByTopic(Integer topicId);

    void submitLesson(int lessonId, SubmitLessonRequest submitLesson);
}