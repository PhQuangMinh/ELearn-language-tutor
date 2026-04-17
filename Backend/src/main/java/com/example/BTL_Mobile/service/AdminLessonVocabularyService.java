package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminFlashCardCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminWordCreateRequest;
import com.example.BTL_Mobile.dto.response.AdminWordResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.DictionaryWord;
import com.example.BTL_Mobile.model.FlashCard;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.DictionaryWordRepository;
import com.example.BTL_Mobile.repository.FlashCardRepository;
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

    private final TopicRepository topicRepository;
    private final DictionaryWordRepository dictionaryWordRepository;
    private final FlashCardRepository flashCardRepository;
    private final AdminLessonFlashCardService adminLessonFlashCardService;

    public List<AdminWordResponse> listWords(Integer topicId) {
        Topic topic = requireTopic(topicId);

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
    public AdminWordResponse addWord(Integer topicId, AdminWordCreateRequest request) {
        Topic topic = requireTopic(topicId);

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

        createAutoFlashCardForWord(topicId, dictionaryWord, request);

        return toResponse(dictionaryWord);
    }

    /**
     * Mỗi lần {@link #addWord} (form admin hoặc import Excel): luôn tạo thêm một flashcard trong đúng topic,
     * gắn {@code dictionaryWordId} tương ứng — example = meaning, ảnh placeholder (admin sửa sau nếu cần).
     */
    private void createAutoFlashCardForWord(Integer topicId, DictionaryWord dictionaryWord, AdminWordCreateRequest request) {
        Integer dwId = dictionaryWord.getId();
        if (dwId == null) {
            return;
        }

        AdminFlashCardCreateRequest fcReq = new AdminFlashCardCreateRequest();
        fcReq.setDictionaryWordId(dwId);
        fcReq.setExample(request.getMeaning().trim());
        fcReq.setImageUrl(AdminLessonFlashCardService.FLASHCARD_PLACEHOLDER_IMAGE_URL);
        fcReq.setImageName("auto_from_word");
        fcReq.setImageSize(0);
        adminLessonFlashCardService.addFlashCard(topicId, fcReq);
    }

    @Transactional
    public AdminWordResponse updateWord(Integer topicId, Integer wordId, AdminWordCreateRequest request) {
        Topic topic = requireTopic(topicId);

        DictionaryWord word = dictionaryWordRepository.findById(wordId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy word", "WORD_NOT_FOUND"));
        if (!isWordLinkedToTopic(topic, wordId)) {
            throw new BusinessException("Word không thuộc topic này", "WORD_NOT_IN_TOPIC");
        }

        String normalizedWord = request.getWord().trim();
        word.setWord(normalizedWord);
        word.setPronunciation(request.getPronunciation().trim());
        word.setMeaning(request.getMeaning().trim());
        word.setType(request.getType());
        DictionaryWord saved = dictionaryWordRepository.save(word);
        return toResponse(saved);
    }

    @Transactional
    public void removeWordFromTopic(Integer topicId, Integer wordId) {
        Topic topic = requireTopic(topicId);

        if (!isWordLinkedToTopic(topic, wordId)) {
            throw new BusinessException("Word không thuộc topic này", "WORD_NOT_IN_TOPIC");
        }

        List<FlashCard> cardsInTopic = flashCardRepository.findByTopicId(topic.getId());
        List<FlashCard> toDelete = cardsInTopic.stream()
                .filter(fc -> fc.getDictionaryWord() != null && wordId.equals(fc.getDictionaryWord().getId()))
                .toList();
        if (!toDelete.isEmpty()) {
            flashCardRepository.deleteAll(toDelete);
        }

        if (topic.getVocabulary() != null) {
            topic.getVocabulary().removeIf(w -> w.getId() != null && w.getId().equals(wordId));
        }
        topicRepository.save(topic);
    }

    private Topic requireTopic(Integer topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
    }

    private boolean isWordLinkedToTopic(Topic topic, Integer wordId) {
        if (topic.getVocabulary() == null || topic.getVocabulary().isEmpty()) {
            return false;
        }
        return topic.getVocabulary().stream()
                .anyMatch(w -> w.getId() != null && w.getId().equals(wordId));
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
