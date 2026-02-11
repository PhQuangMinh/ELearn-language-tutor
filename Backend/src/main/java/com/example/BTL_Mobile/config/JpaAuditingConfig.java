package com.example.BTL_Mobile.config;

import com.example.BTL_Mobile.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Integer> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
                return Optional.of(0);
            }

            Object principal = auth.getPrincipal();
            if (principal instanceof User user && user.getId() != null) {
                return Optional.of(user.getId());
            }

            return Optional.of(0);
        };
    }
}

