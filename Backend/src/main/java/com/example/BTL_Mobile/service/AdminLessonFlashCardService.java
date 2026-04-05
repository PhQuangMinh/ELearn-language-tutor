package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminFlashCardCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminFlashCardUpdateRequest;
import com.example.BTL_Mobile.dto.response.FlashCardResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.DictionaryWord;
import com.example.BTL_Mobile.model.FlashCard;
import com.example.BTL_Mobile.model.Media;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.model.enums.EMediaType;
import com.example.BTL_Mobile.repository.DictionaryWordRepository;
import com.example.BTL_Mobile.repository.FlashCardRepository;
import com.example.BTL_Mobile.repository.MediaRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLessonFlashCardService {

    private final TopicRepository topicRepository;
    private final DictionaryWordRepository dictionaryWordRepository;
    private final MediaRepository mediaRepository;
    private final FlashCardRepository flashCardRepository;

    public List<FlashCardResponse> listFlashCards(Integer topicId) {
        Topic topic = requireTopic(topicId);
        return flashCardRepository.findByTopicId(topic.getId()).stream()
                .sorted(Comparator.comparing(FlashCard::getId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FlashCardResponse addFlashCard(Integer topicId, AdminFlashCardCreateRequest request) {
        Topic topic = requireTopic(topicId);

        DictionaryWord dictionaryWord = resolveDictionaryWord(request);

        if (topic.getVocabulary() == null) {
            topic.setVocabulary(new HashSet<>());
        }
        topic.getVocabulary().add(dictionaryWord);
        topicRepository.save(topic);

        Media media = new Media();
        media.setType(EMediaType.IMAGE);
        media.setUrl(request.getImageUrl().trim());
        media.setName((request.getImageName() == null || request.getImageName().isBlank())
                ? "flashcard_image"
                : request.getImageName().trim());
        media.setSize(request.getImageSize() == null ? 0 : request.getImageSize());
        Media savedMedia = mediaRepository.save(media);

        FlashCard flashCard = new FlashCard();
        flashCard.setDictionaryWord(dictionaryWord);
        flashCard.setMedia(savedMedia);
        flashCard.setExample(request.getExample().trim());
        FlashCard saved = flashCardRepository.save(flashCard);

        return toResponse(saved);
    }

    @Transactional
    public FlashCardResponse updateFlashCard(Integer topicId, Integer flashCardId, AdminFlashCardUpdateRequest request) {
        Topic topic = requireTopic(topicId);

        FlashCard flashCard = flashCardRepository.findById(flashCardId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy flashcard", "FLASHCARD_NOT_FOUND"));
        requireFlashCardInTopic(topic, flashCard);

        boolean hasChange = false;

        if (request.getExample() != null && !request.getExample().isBlank()) {
            flashCard.setExample(request.getExample().trim());
            hasChange = true;
        }

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            Media media = new Media();
            media.setType(EMediaType.IMAGE);
            media.setUrl(request.getImageUrl().trim());
            media.setName((request.getImageName() == null || request.getImageName().isBlank())
                    ? "flashcard_image"
                    : request.getImageName().trim());
            media.setSize(request.getImageSize() == null ? 0 : request.getImageSize());
            Media savedMedia = mediaRepository.save(media);
            flashCard.setMedia(savedMedia);
            hasChange = true;
        }

        boolean wordChangeRequested = request.getDictionaryWordId() != null
                || (request.getWord() != null && !request.getWord().isBlank());
        if (wordChangeRequested) {
            AdminFlashCardCreateRequest resolveReq = new AdminFlashCardCreateRequest();
            resolveReq.setDictionaryWordId(request.getDictionaryWordId());
            resolveReq.setWord(request.getWord());
            resolveReq.setPronunciation(request.getPronunciation());
            resolveReq.setMeaning(request.getMeaning());
            resolveReq.setType(request.getType());
            DictionaryWord dictionaryWord = resolveDictionaryWord(resolveReq);
            if (topic.getVocabulary() == null) {
                topic.setVocabulary(new HashSet<>());
            }
            topic.getVocabulary().add(dictionaryWord);
            topicRepository.save(topic);
            flashCard.setDictionaryWord(dictionaryWord);
            hasChange = true;
        }

        if (!hasChange) {
            throw new BusinessException("Không có dữ liệu cập nhật", "NO_CHANGES");
        }

        FlashCard saved = flashCardRepository.save(flashCard);
        return toResponse(saved);
    }

    @Transactional
    public void deleteFlashCard(Integer topicId, Integer flashCardId) {
        Topic topic = requireTopic(topicId);

        FlashCard flashCard = flashCardRepository.findById(flashCardId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy flashcard", "FLASHCARD_NOT_FOUND"));
        requireFlashCardInTopic(topic, flashCard);
        flashCardRepository.delete(flashCard);
    }

    private Topic requireTopic(Integer topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy topic", "TOPIC_NOT_FOUND"));
    }

    private void requireFlashCardInTopic(Topic topic, FlashCard flashCard) {
        DictionaryWord dw = flashCard.getDictionaryWord();
        if (dw == null || dw.getId() == null) {
            throw new BusinessException("Flashcard dữ liệu không hợp lệ", "FLASHCARD_INVALID");
        }
        if (topic.getVocabulary() == null || topic.getVocabulary().isEmpty()) {
            throw new BusinessException("Flashcard không thuộc topic này", "FLASHCARD_NOT_IN_TOPIC");
        }
        boolean linked = topic.getVocabulary().stream()
                .anyMatch(w -> w.getId() != null && w.getId().equals(dw.getId()));
        if (!linked) {
            throw new BusinessException("Flashcard không thuộc topic này", "FLASHCARD_NOT_IN_TOPIC");
        }
    }

    private DictionaryWord resolveDictionaryWord(AdminFlashCardCreateRequest request) {
        if (request.getDictionaryWordId() != null) {
            return dictionaryWordRepository.findById(request.getDictionaryWordId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy word", "WORD_NOT_FOUND"));
        }

        if (request.getWord() == null || request.getWord().isBlank()
                || request.getPronunciation() == null || request.getPronunciation().isBlank()
                || request.getMeaning() == null || request.getMeaning().isBlank()
                || request.getType() == null) {
            throw new BusinessException("Thiếu thông tin word để tạo flashcard", "WORD_REQUIRED");
        }

        String normalizedWord = request.getWord().trim();
        return dictionaryWordRepository.findByWordIgnoreCase(normalizedWord)
                .orElseGet(() -> {
                    DictionaryWord created = new DictionaryWord();
                    created.setWord(normalizedWord);
                    created.setPronunciation(request.getPronunciation().trim());
                    created.setMeaning(request.getMeaning().trim());
                    created.setType(request.getType());
                    return dictionaryWordRepository.save(created);
                });
    }

    private FlashCardResponse toResponse(FlashCard flashCard) {
        var dictionaryWord = flashCard.getDictionaryWord();
        var media = flashCard.getMedia();

        return FlashCardResponse.builder()
                .id(flashCard.getId())
                .word(dictionaryWord != null ? dictionaryWord.getWord() : null)
                .pronunciation(dictionaryWord != null ? dictionaryWord.getPronunciation() : null)
                .meaning(dictionaryWord != null ? dictionaryWord.getMeaning() : null)
                .example(flashCard.getExample())
                .imageUrl(media != null ? media.getUrl() : null)
                .build();
    }
}
