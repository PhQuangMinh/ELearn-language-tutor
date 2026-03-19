package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.ScenarioDetailResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Scenario;
import com.example.BTL_Mobile.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    public ScenarioDetailResponse getScenarioByLessonId(int lessonId) {
        Scenario scenario = scenarioRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new BusinessException("Scenario not found for this lesson", "SCENARIO_NOT_FOUND"));

        return ScenarioDetailResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .tasks(scenario.getTasks())
                .openningMessage(scenario.getOpenningMessage())
                .suggestion(scenario.getSuggestion())
                .translation(scenario.getTranslation())
                .build();
    }
}

