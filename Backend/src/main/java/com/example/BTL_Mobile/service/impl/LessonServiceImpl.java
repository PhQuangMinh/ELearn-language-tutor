package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    @Override
    public Optional<Lesson> getLessonById(int id) {
        return lessonRepository.findById(id);
    }

}
