package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminScenarioCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminScenarioUpdateRequest;
import com.example.BTL_Mobile.dto.response.AdminScenarioResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.Scenario;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.ScenarioRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;

    public Page<AdminScenarioResponse> list(Pageable pageable) {
        return scenarioRepository.findAll(pageable).map(this::toResponse);
    }

    public AdminScenarioResponse getById(Integer id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy scenario", "SCENARIO_NOT_FOUND"));
        return toResponse(scenario);
    }

    @Transactional
    public AdminScenarioResponse create(AdminScenarioCreateRequest request) {
        Topic topic = resolveTopic(request.getTopicId());
        Lesson lesson = resolveLesson(request.getLessonId());
        ensureLessonBelongsToTopic(lesson, topic.getId());

        Scenario scenario = new Scenario();
        scenario.setTopic(topic);
        scenario.setLessonId(lesson.getId());
        scenario.setTitle(request.getTitle());
        scenario.setDescription(request.getDescription());
        scenario.setAiRole(request.getAiRole());
        scenario.setUserRole(request.getUserRole());
        scenario.setTasks(request.getTasks());
        scenario.setOpenningMessage(request.getOpenningMessage());
        scenario.setSuggestion(request.getSuggestion());
        scenario.setTranslation(request.getTranslation());

        Scenario saved = scenarioRepository.save(scenario);
        return toResponse(saved);
    }

    @Transactional
    public AdminScenarioResponse update(Integer id, AdminScenarioUpdateRequest request) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy scenario", "SCENARIO_NOT_FOUND"));

        Integer topicId = request.getTopicId() != null ? request.getTopicId() : scenario.getTopic().getId();
        Integer lessonId = request.getLessonId() != null ? request.getLessonId() : scenario.getLessonId();

        Topic topic = resolveTopic(topicId);
        Lesson lesson = resolveLesson(lessonId);
        ensureLessonBelongsToTopic(lesson, topic.getId());

        scenario.setTopic(topic);
        scenario.setLessonId(lesson.getId());

        if (request.getTitle() != null) {
            scenario.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            scenario.setDescription(request.getDescription());
        }
        if (request.getAiRole() != null) {
            scenario.setAiRole(request.getAiRole());
        }
        if (request.getUserRole() != null) {
            scenario.setUserRole(request.getUserRole());
        }
        if (request.getTasks() != null) {
            scenario.setTasks(request.getTasks());
        }
        if (request.getOpenningMessage() != null) {
            scenario.setOpenningMessage(request.getOpenningMessage());
        }
        if (request.getSuggestion() != null) {
            scenario.setSuggestion(request.getSuggestion());
        }
        if (request.getTranslation() != null) {
            scenario.setTranslation(request.getTranslation());
        }

        Scenario saved = scenarioRepository.save(scenario);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!scenarioRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy scenario", "SCENARIO_NOT_FOUND");
        }
        scenarioRepository.deleteById(id);
    }

    @Transactional
    public List<AdminScenarioResponse> bulkImport(List<AdminScenarioCreateRequest> requests) {
        return requests.stream()
                .map(this::create)
                .toList();
    }

    private Topic resolveTopic(Integer topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
    }

    private Lesson resolveLesson(Integer lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND"));
    }

    private void ensureLessonBelongsToTopic(Lesson lesson, Integer topicId) {
        Integer lessonTopicId = lesson.getTopic() == null ? null : lesson.getTopic().getId();
        if (lessonTopicId == null || !lessonTopicId.equals(topicId)) {
            throw new BusinessException("Lesson không thuộc topic đã chọn", "LESSON_TOPIC_MISMATCH");
        }
    }

    private AdminScenarioResponse toResponse(Scenario scenario) {
        return AdminScenarioResponse.builder()
                .id(scenario.getId())
                .topicId(scenario.getTopic() == null ? null : scenario.getTopic().getId())
                .lessonId(scenario.getLessonId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .aiRole(scenario.getAiRole())
                .userRole(scenario.getUserRole())
                .tasks(scenario.getTasks())
                .openningMessage(scenario.getOpenningMessage())
                .suggestion(scenario.getSuggestion())
                .translation(scenario.getTranslation())
                .build();
    }
}