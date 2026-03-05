package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.FlashCardResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.FlashCard;
import com.example.BTL_Mobile.repository.FlashCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashCardService {

    private final FlashCardRepository flashCardRepository;

    public Page<FlashCardResponse> getFlashCards(Pageable pageable) {
        return flashCardRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public List<FlashCardResponse> getAllFlashCards() {
        return flashCardRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FlashCardResponse getById(Integer id) {
        FlashCard flashCard = flashCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy flashcard", "FLASHCARD_NOT_FOUND"));
        return mapToResponse(flashCard);
    }

    private FlashCardResponse mapToResponse(FlashCard flashCard) {
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

