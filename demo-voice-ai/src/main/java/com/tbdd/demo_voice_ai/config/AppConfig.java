package com.tbdd.demo_voice_ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureSpeechProperties.class)
public class AppConfig {}

