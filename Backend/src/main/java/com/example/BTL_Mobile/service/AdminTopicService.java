package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminTopicCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminTopicUpdateRequest;
import com.example.BTL_Mobile.dto.response.TopicResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTopicService {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;

    public Page<TopicResponse> list(Pageable pageable) {
        return topicRepository.findAll(pageable).map(this::toTopicResponse);
    }

    public TopicResponse getById(Integer id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
        return toTopicResponse(topic);
    }

    @Transactional
    public TopicResponse create(AdminTopicCreateRequest request) {
        Topic topic = new Topic();
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setImageUrl(request.getImageUrl());
        Topic saved = topicRepository.save(topic);
        return toTopicResponse(saved);
    }

    @Transactional
    public TopicResponse update(Integer id, AdminTopicUpdateRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
        if (request.getName() != null) {
            topic.setName(request.getName());
        }
        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            topic.setImageUrl(request.getImageUrl());
        }
        Topic saved = topicRepository.save(topic);
        return toTopicResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!topicRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND");
        }
        lessonRepository.findByTopicIdOrderByIdAsc(id).forEach(lessonRepository::delete);
        topicRepository.deleteById(id);
    }

    @Transactional
    public List<TopicResponse> bulkImport(List<AdminTopicCreateRequest> requests) {
        return requests.stream()
                .map(this::create)
                .toList();
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
            .imageUrl(topic.getImageUrl())
                .build();
    }
}
