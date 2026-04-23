package com.example.BTL_Mobile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();
  }
}
