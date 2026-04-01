package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminWordCreateRequest;
import com.example.BTL_Mobile.dto.response.AdminWordResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.DictionaryWord;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.DictionaryWordRepository;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLessonVocabularyService {

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;
    private final DictionaryWordRepository dictionaryWordRepository;

    public List<AdminWordResponse> listWords(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND"));
        Topic topic = lesson.getTopic();
        if (topic == null) {
            return List.of();
        }

        var vocab = topic.getVocabulary();
        if (vocab == null || vocab.isEmpty()) {
            return List.of();
        }

        return vocab.stream()
                .sorted(Comparator.comparing(DictionaryWord::getId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminWordResponse addWord(Integer lessonId, AdminWordCreateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lesson", "LESSON_NOT_FOUND"));
        Topic topic = lesson.getTopic();
        if (topic == null) {
            throw new BusinessException("Lesson chưa gắn topic", "LESSON_TOPIC_REQUIRED");
        }

        String normalizedWord = request.getWord().trim();

        DictionaryWord dictionaryWord = dictionaryWordRepository.findByWordIgnoreCase(normalizedWord)
                .orElseGet(() -> {
                    DictionaryWord created = new DictionaryWord();
                    created.setWord(normalizedWord);
                    created.setPronunciation(request.getPronunciation().trim());
                    created.setMeaning(request.getMeaning().trim());
                    created.setType(request.getType());
                    return dictionaryWordRepository.save(created);
                });

        if (topic.getVocabulary() == null) {
            topic.setVocabulary(new HashSet<>());
        }
        topic.getVocabulary().add(dictionaryWord);
        topicRepository.save(topic);

        return toResponse(dictionaryWord);
    }

    private AdminWordResponse toResponse(DictionaryWord word) {
        return AdminWordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .pronunciation(word.getPronunciation())
                .meaning(word.getMeaning())
                .type(word.getType())
                .build();
    }
}

