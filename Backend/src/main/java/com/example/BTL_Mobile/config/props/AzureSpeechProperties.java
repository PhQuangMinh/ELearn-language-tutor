package com.example.BTL_Mobile.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.speech")
public record AzureSpeechProperties(
        String key,
        String region,
        String defaultLanguage
) {}
