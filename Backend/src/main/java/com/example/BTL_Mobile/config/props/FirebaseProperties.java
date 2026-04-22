package com.example.BTL_Mobile.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private String credentialsPath;

    private String projectId;
}
