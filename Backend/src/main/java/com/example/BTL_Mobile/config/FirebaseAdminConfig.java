package com.example.BTL_Mobile.config;

import com.example.BTL_Mobile.config.props.FirebaseProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseAdminConfig {

    @Bean
    @ConditionalOnExpression("'${firebase.credentials-path:}'.length() > 0")
    public FirebaseApp firebaseApp(FirebaseProperties firebaseProperties) throws IOException {
        try (FileInputStream serviceAccount = new FileInputStream(firebaseProperties.getCredentialsPath())) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount));

            String projectId = firebaseProperties.getProjectId();
            if (projectId != null && !projectId.isBlank()) {
                optionsBuilder.setProjectId(projectId);
            }

            FirebaseOptions options = optionsBuilder.build();
            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            }
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
