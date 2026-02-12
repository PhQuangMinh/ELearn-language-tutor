package com.example.BTL_Mobile.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ELearn Language Tutor API",
                version = "v1",
                description = "Swagger UI để test nhanh các API (auth, oauth2, ...)."
        )
)
@SecurityScheme(
                name = OpenApiConfig.BEARER_AUTH,
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

        public static final String BEARER_AUTH = "bearerAuth";

        @Bean
        public OpenApiCustomizer jwtAuthOpenApiCustomizer() {
                return openApi -> {
                        if (openApi.getPaths() == null) return;

                        for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
                                String path = entry.getKey();
                                PathItem pathItem = entry.getValue();
                                if (pathItem == null) continue;

                                boolean shouldSecure = shouldSecure(path);
                                if (!shouldSecure) continue;

                                addSecurity(pathItem.getGet());
                                addSecurity(pathItem.getPost());
                                addSecurity(pathItem.getPut());
                                addSecurity(pathItem.getDelete());
                                addSecurity(pathItem.getPatch());
                                addSecurity(pathItem.getHead());
                                addSecurity(pathItem.getOptions());
                                addSecurity(pathItem.getTrace());
                        }
                };
        }

        private static void addSecurity(Operation operation) {
                if (operation == null) return;

                List<SecurityRequirement> security = operation.getSecurity();
                if (security == null || security.isEmpty()) {
                        operation.setSecurity(List.of(new SecurityRequirement().addList(BEARER_AUTH)));
                        return;
                }

                boolean alreadyHas = security.stream().anyMatch(req -> req.containsKey(BEARER_AUTH));
                if (!alreadyHas) {
                        security.add(new SecurityRequirement().addList(BEARER_AUTH));
                }
        }

        private static boolean shouldSecure(String path) {
                // Public/auth endpoints should NOT require Authorization header in Swagger
                if (path == null) return false;

                if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) return false;
                if (path.startsWith("/oauth2") || path.startsWith("/login/oauth2")) return false;
                if (path.equals("/api/auth/register")) return false;
                if (path.equals("/api/auth/register/verify")) return false;
                if (path.equals("/api/auth/forgot-password")) return false;
                if (path.equals("/api/auth/forgot-password/verify")) return false;
                if (path.equals("/api/auth/reset-password")) return false;
                if (path.equals("/api/auth/login")) return false;
                if (path.equals("/api/auth/oauth2/google")) return false;
                if (path.equals("/api/auth/refresh")) return false;
                if (path.equals("/api/auth/logout")) return false;
                if (path.equals("/api/auth/test")) return false;
                if (path.equals("/api/auth/validate")) return false;
                if (path.startsWith("/api/auth/oauth2/authorize")) return false;

                // Everything else under /api/** is secured
                return path.startsWith("/api/");
        }
}

