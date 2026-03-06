package com.example.BTL_Mobile.dto.response.topic;

import com.example.BTL_Mobile.dto.response.WordDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TopicVocabularyDTO {

    private int topicId;

    private List<WordDTO> vocabulary;

}
