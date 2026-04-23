package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.service.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceImpl implements GeminiService {

  private final ObjectMapper objectMapper;

  private final HttpClient httpClient;

  @Value("${gemini.api-key:}")
  private String geminiKey;

  @Value("${gemini.model:gemini-2.5-flash}")
  private String geminiModel;

  @Override
  public Object callGemini(String prompt) {
    log.info("gemini key: {}", geminiKey);
    log.info("gemini model: {}", geminiModel);
    if (prompt == null || prompt.isBlank()) {
      throw new IllegalArgumentException("prompt must not be blank");
    }

    if (geminiKey == null || geminiKey.isBlank()) {
      throw new IllegalStateException("Missing config: gemini.api-key (set GEMINI_API_KEY in .env)");
    }

    String apiUrl = String.format(
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
        geminiModel,
        geminiKey
    );

    ObjectNode requestBody = makeBodyRequest(prompt);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new RuntimeException(
            "Gemini request failed: status=" + response.statusCode() + ", body=" + response.body());
      }

      JsonNode root = objectMapper.readTree(response.body());
      String text = root.path("candidates").isArray() && !root.path("candidates").isEmpty()
          ? root.path("candidates").get(0).path("content").path("parts").isArray()
          && !root.path("candidates").get(0).path("content").path("parts").isEmpty()
          ? root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
          .asText(null)
          : null
          : null;

      if (text == null || text.isBlank()) {
        return root;
      }

      try {
        return objectMapper.readTree(text);
      } catch (Exception parseJsonEx) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.put("raw", text);
        return wrapper;
      }
    } catch (Exception e) {
      log.error("Call Gemini failed", e);
      throw new RuntimeException("Call Gemini failed: " + e.getMessage(), e);
    }
  }

  @Override
  public <T> T callGemini(String prompt, Class<T> responseType) {
    Object result = callGemini(prompt);
    if (result == null) {
      return null;
    }
    return objectMapper.convertValue(result, responseType);
  }

  private ObjectNode makeBodyRequest(String prompt) {

    ObjectNode partsNode = objectMapper.createObjectNode();
    partsNode.put("text", prompt);

    ArrayNode partsArray = objectMapper.createArrayNode().add(partsNode);
    ObjectNode contentNode = objectMapper.createObjectNode();
    contentNode.set("parts", partsArray);

    ArrayNode contentsArray = objectMapper.createArrayNode().add(contentNode);

    ObjectNode requestBodyNode = objectMapper.createObjectNode();
    requestBodyNode.set("contents", contentsArray);

    ObjectNode generationConfig = objectMapper.createObjectNode();
    generationConfig.put("response_mime_type", "application/json");
    generationConfig.put("temperature", 0.7);
    requestBodyNode.set("generationConfig", generationConfig);

    ObjectNode safetySetting = objectMapper.createObjectNode();
    safetySetting.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
    safetySetting.put("threshold", "BLOCK_ONLY_HIGH");
    ArrayNode safetySettingsArray = objectMapper.createArrayNode().add(safetySetting);
    requestBodyNode.set("safetySettings", safetySettingsArray);

    return requestBodyNode;
  }
}

