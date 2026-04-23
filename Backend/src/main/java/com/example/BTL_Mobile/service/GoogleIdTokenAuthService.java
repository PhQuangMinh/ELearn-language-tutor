package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.AuthResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class GoogleIdTokenAuthService {

    private final OAuth2Service oAuth2Service;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    private final RestClient googleRestClient = RestClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build();

    public AuthResponse loginWithGoogleIdToken(String idToken) {
        GoogleTokenInfo tokenInfo = verifyWithGoogleTokenInfo(idToken);

        if (tokenInfo.aud() == null || tokenInfo.aud().isBlank()) {
            throw new BusinessException("Google token is invalid", "INVALID_GOOGLE_TOKEN");
        }
        if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(tokenInfo.aud())) {
            throw new BusinessException("Google token audience mismatch", "INVALID_GOOGLE_TOKEN_AUD");
        }
        if (tokenInfo.sub() == null || tokenInfo.sub().isBlank()) {
            throw new BusinessException("Google token is invalid", "INVALID_GOOGLE_TOKEN");
        }

        // For Google: sub is stable unique user id.
        return oAuth2Service.findOrCreateAndIssueTokens(
                tokenInfo.sub(),
                tokenInfo.email(),
                tokenInfo.name(),
                "google"
        );
    }

    private GoogleTokenInfo verifyWithGoogleTokenInfo(String idToken) {
        try {
            return googleRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("id_token", idToken).build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new BusinessException("Google token is invalid", "INVALID_GOOGLE_TOKEN");
                    })
                    .body(GoogleTokenInfo.class);
        } catch (RestClientResponseException ex) {
            throw new BusinessException("Google token is invalid", "INVALID_GOOGLE_TOKEN");
        }
    }

    public record GoogleTokenInfo(
            String sub,
            String email,
            String name,
            String aud,
            @JsonProperty("email_verified") Boolean emailVerified
    ) {
    }
}

