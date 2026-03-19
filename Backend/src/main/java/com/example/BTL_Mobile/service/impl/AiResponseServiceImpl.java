package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.request.AiRespondRequest;
import com.example.BTL_Mobile.dto.response.AiRespondResponse;
import com.example.BTL_Mobile.service.AiResponseService;
import com.example.BTL_Mobile.service.GeminiService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiResponseServiceImpl implements AiResponseService {

  private final GeminiService geminiService;
  private final ResourceLoader loader;

  private String systemPrompt;

  @PostConstruct
  public void init() {
    try {
      systemPrompt = getPromptFromFile("classpath:/data/ai-response-prompt.txt");
    } catch (IOException e) {
      throw new IllegalStateException("Cannot load ai-response-prompt.txt", e);
    }
  }

  @NotNull
  private String getPromptFromFile(String path) throws IOException {
    Resource resource = loader.getResource(path);
    byte[] buffer = FileCopyUtils.copyToByteArray(resource.getInputStream());
    return new String(buffer, StandardCharsets.UTF_8);
  }

  @Override
  public AiRespondResponse respond(AiRespondRequest request) {
    String fullPrompt = buildFullPrompt(request);
    log.info("AiResponse prompt size={}", fullPrompt);

    return geminiService.callGemini(fullPrompt, AiRespondResponse.class);
  }

  private String buildFullPrompt(AiRespondRequest request) {
    return String.format("""
            %s
            
            SCENARIO:
            %s
            
            TASK:
            %s
            
            HISTORY:
            %s
            
            USER_MESSAGE:
            %s
            """,
        systemPrompt,
        request.getScenarioDescription(),
        request.getTaskDescription(),
        request.getConversationHistory(),
        request.getUserMessage()
    );
  }
}

