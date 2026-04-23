package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.topic.TopicVocabularyDTO;
import com.example.BTL_Mobile.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topic")
public class TopicController {

  private final TopicService topicService;

  @GetMapping("/{topicId}/vocabularies")
  public ResponseEntity<TopicVocabularyDTO> getVocabularies(@PathVariable int topicId) {
    return ResponseEntity.ok(topicService.getVocabulary(topicId));
  }

}
