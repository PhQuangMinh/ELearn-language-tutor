package com.example.BTL_Mobile.config.props;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
	AzureSpeechProperties.class,
	FirebaseProperties.class
})
public class AppConfig {
}
