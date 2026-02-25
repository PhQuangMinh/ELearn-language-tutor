package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.model.Lesson;

import java.util.Optional;

public interface LessonService {

    Optional<Lesson> getLessonById(int id);

}
