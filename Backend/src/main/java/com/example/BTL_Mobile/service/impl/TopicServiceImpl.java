package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.response.WordDTO;
import com.example.BTL_Mobile.dto.response.topic.TopicVocabularyDTO;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.mapper.DictionaryWordMapper;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.repository.TopicRepository;
import com.example.BTL_Mobile.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    public final TopicRepository topicRepository;

    private final DictionaryWordMapper dictionaryWordMapper;

    @Override
    public TopicVocabularyDTO getVocabulary(int topicId){
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Topic not found"));
        List<WordDTO> wordDTOS = topic.getVocabulary().stream()
                .map(dictionaryWordMapper::toWordDTO)
                .toList();
        TopicVocabularyDTO res = new TopicVocabularyDTO();
        res.setTopicId(topicId);
        res.setVocabulary(wordDTOS);
        return res;
    }

}
