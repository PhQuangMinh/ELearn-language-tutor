package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminLessonCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminLessonUpdateRequest;
import com.example.BTL_Mobile.dto.response.AdminLessonResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLessonService {

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;

    public Page<AdminLessonResponse> list(Pageable pageable) {
        return lessonRepository.findAll(pageable).map(this::toResponse);
    }

    public AdminLessonResponse getById(Integer id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND"));
        return toResponse(lesson);
    }

    @Transactional
    public AdminLessonResponse create(AdminLessonCreateRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));

        Lesson lesson = new Lesson();
        lesson.setTopic(topic);
        lesson.setTitle(request.getTitle());
        lesson.setType(request.getType());
        lesson.setImageUrl(request.getImageUrl());

        if (request.getParentId() != null) {
            lesson.setParent(resolveParentLesson(request.getParentId()));
        }

        Lesson saved = lessonRepository.save(lesson);
        return toResponse(saved);
    }

    @Transactional
    public AdminLessonResponse update(Integer id, AdminLessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND"));

        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
            lesson.setTopic(topic);
        }

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }

        if (request.getType() != null) {
            lesson.setType(request.getType());
        }

        if (request.getImageUrl() != null) {
            lesson.setImageUrl(request.getImageUrl());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("Lesson cha không hợp lệ", "INVALID_PARENT_LESSON");
            }
            lesson.setParent(resolveParentLesson(request.getParentId()));
        }

        Lesson saved = lessonRepository.save(lesson);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!lessonRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND");
        }
        lessonRepository.deleteById(id);
    }

    private Lesson resolveParentLesson(Integer parentId) {
        return lessonRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson cha", "PARENT_LESSON_NOT_FOUND"));
    }

    private AdminLessonResponse toResponse(Lesson lesson) {
        return AdminLessonResponse.builder()
                .id(lesson.getId())
                .topicId(lesson.getTopic() == null ? null : lesson.getTopic().getId())
                .title(lesson.getTitle())
                .type(lesson.getType() == null ? null : lesson.getType().name())
                .imageUrl(lesson.getImageUrl())
                .parentId(lesson.getParent() == null ? null : lesson.getParent().getId())
                .build();
    }
}