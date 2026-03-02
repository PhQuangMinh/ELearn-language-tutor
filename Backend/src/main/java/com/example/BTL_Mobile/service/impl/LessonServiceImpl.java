package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.response.LessonInTopicResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import com.example.BTL_Mobile.repository.UserLessonResultRepository;
import com.example.BTL_Mobile.security.CurrentUserContext;
import com.example.BTL_Mobile.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonResultRepository userLessonResultRepository;
    private final CurrentUserContext currentUserContext;

    @Override
    public Optional<Lesson> getLessonById(int id) {
        return lessonRepository.findById(id);
    }

    @Override
    public List<LessonInTopicResponse> getLessonsByTopic(Integer topicId) {
        if (topicId == null) {
            throw new BusinessException("TopicId không hợp lệ", "INVALID_TOPIC_ID");
        }

        if (!topicRepository.existsById(topicId)) {
            throw new BusinessException("Topic không tồn tại", "TOPIC_NOT_FOUND");
        }

        Integer userId = currentUserContext.requireUserId();

        List<Lesson> lessons = lessonRepository.findByTopic_IdOrderByIdAsc(topicId);
        Set<Integer> completedLessonIds = new HashSet<>(
            userLessonResultRepository.findCompletedLessonIdsInTopic(userId, topicId)
        );

        return lessons.stream()
            .map(lesson -> LessonInTopicResponse.builder()
                .id(lesson.getId())
                .name(lesson.getTitle())
                .imageUrl(lesson.getImageUrl())
                .completed(completedLessonIds.contains(lesson.getId()))
                .build())
            .toList();
    }

}
