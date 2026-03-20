package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AiRespondRequest;
import com.example.BTL_Mobile.dto.response.AiRespondResponse;
import com.example.BTL_Mobile.dto.response.InitSpeakingSessionResponse;
import com.example.BTL_Mobile.dto.response.ScenarioDetailResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Scenario;
import com.example.BTL_Mobile.model.SpeakingMessage;
import com.example.BTL_Mobile.model.SpeakingSession;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.model.enums.EMessageSender;
import com.example.BTL_Mobile.repository.ScenarioRepository;
import com.example.BTL_Mobile.repository.SpeakingMessageRepository;
import com.example.BTL_Mobile.repository.SpeakingSessionRepository;
import com.example.BTL_Mobile.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    private final SpeakingSessionRepository speakingSessionRepository;

    private final SpeakingMessageRepository speakingMessageRepository;

    private final CurrentUserContext userContext;

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

    public void saveMessages(AiRespondRequest request, AiRespondResponse response){
        SpeakingSession session = speakingSessionRepository.findById(request.getSpeakingSessionId())
                .orElseThrow(() -> new BusinessException("Session not found", "SPEAKING_SESSION_NOT_FOUND"));
        SpeakingMessage userMessage = SpeakingMessage.builder()
                .speakingSession(session)
                .content(request.getUserMessage())
                .duration(0)
                .sender(EMessageSender.USER)
                .grammarScore(100.0)
                .vocabularyScore(100.0)
                .pronunciationScore(100.0)
                .build();
        SpeakingMessage aiMessage = SpeakingMessage.builder()
                .speakingSession(session)
                .content(response.getAiMessage())
                .duration(0)
                .sender(EMessageSender.AI)
                .vocabularyScore(0.0)
                .grammarScore(0.0)
                .pronunciationScore(0.0)
                .build();
        speakingMessageRepository.saveAll(List.of(userMessage, aiMessage));
    }

    public InitSpeakingSessionResponse initSpeakingSession(int scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new BusinessException("Scenario not found for this lesson", "SCENARIO_NOT_FOUND"));
        int userId = userContext.getCurrentUserId().get();
        User user = User.builder().id(userId).build();
        SpeakingSession newSession = SpeakingSession.builder()
                .user(user)
                .scenario(scenario)
                .startedAt(LocalDateTime.now())
                .grammarScore(0.0)
                .vocabularyScore(0.0)
                .pronunciationScore(0.0)
                .build();
        newSession = speakingSessionRepository.save(newSession);
        return new InitSpeakingSessionResponse(newSession.getId());
    }

    public void endSpeakingSession(int sessionId) {
        SpeakingSession speakingSession = speakingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Session not found", "SPEAKING_SESSION_NOT_FOUND"));

        speakingSession.setEndedAt(LocalDateTime.now());

        List<SpeakingMessage> userMessages = speakingSession.getMessages().stream()
                .filter(m -> m.getSender() == EMessageSender.USER)
                .toList();

        int messageCount = userMessages.size();

        if (messageCount > 0) {
            double grammarAvg = userMessages.stream()
                    .mapToDouble(SpeakingMessage::getGrammarScore)
                    .average()
                    .orElse(0);

            double pronunAvg = userMessages.stream()
                    .mapToDouble(SpeakingMessage::getPronunciationScore)
                    .average()
                    .orElse(0);

            double vocabAvg = userMessages.stream()
                    .mapToDouble(SpeakingMessage::getVocabularyScore)
                    .average()
                    .orElse(0);

            speakingSession.setGrammarScore(grammarAvg);
            speakingSession.setPronunciationScore(pronunAvg);
            speakingSession.setVocabularyScore(vocabAvg);
        }

        speakingSessionRepository.save(speakingSession);
    }

}

