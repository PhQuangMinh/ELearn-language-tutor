package com.tbdd.demo_voice_ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.speech")
public record AzureSpeechProperties(
        String key,
        String region,
        String defaultLanguage
) {}

