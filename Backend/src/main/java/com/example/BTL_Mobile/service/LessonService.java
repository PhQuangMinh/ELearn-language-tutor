package com.example.BTL_Mobile.service;

import java.util.List;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.dto.response.LessonInTopicResponse;

import java.util.Optional;

public interface LessonService {

    Optional<Lesson> getLessonById(int id);
    List<LessonInTopicResponse> getLessonsByTopic(Integer topicId);

}