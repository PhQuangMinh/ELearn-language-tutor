package com.example.BTL_Mobile.config;

import com.example.BTL_Mobile.security.CurrentUserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

  private final CurrentUserContext currentUserContext;

  public JpaAuditingConfig(CurrentUserContext currentUserContext) {
    this.currentUserContext = currentUserContext;
  }

  @Bean
  public AuditorAware<Integer> auditorAware() {
    return () -> {
      return currentUserContext.getCurrentUserId().or(() -> Optional.of(0));
    };
  }
}

