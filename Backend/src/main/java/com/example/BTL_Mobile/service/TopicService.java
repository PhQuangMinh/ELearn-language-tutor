package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.topic.TopicVocabularyDTO;

public interface TopicService {

    TopicVocabularyDTO getVocabulary(int topicId);

}
