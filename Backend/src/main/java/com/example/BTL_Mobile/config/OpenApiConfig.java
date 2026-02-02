package com.example.BTL_Mobile.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ELearn Language Tutor API",
                version = "v1",
                description = "Swagger UI để test nhanh các API (auth, oauth2, ...)."
        )
)
public class OpenApiConfig {
}

