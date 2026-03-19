package com.example.BTL_Mobile.service;

/**
 * Common service to call Gemini API with a prompt.
 * Return type is Object (actual value is parsed JSON as a Jackson node).
 */
public interface GeminiService {

    Object callGemini(String prompt);

    <T> T callGemini(String prompt, Class<T> responseType);
}

