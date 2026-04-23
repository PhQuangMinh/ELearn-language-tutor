package com.example.BTL_Mobile.config;

import com.example.BTL_Mobile.dto.response.AuthResponse;
import com.example.BTL_Mobile.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Sau khi user đăng nhập Google xong, Spring OAuth2 đã đổi code lấy thông tin user. Handler này:
 * tìm/tạo user trong DB, phát JWT + refresh token, redirect về frontend kèm token.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final OAuth2Service oAuth2Service;

  @Value("${oauth2.mobile-redirect-url:elearn://login/callback}")
  private String mobileRedirectUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> attributes = oauth2User.getAttributes();

    String registrationId = getRegistrationId(authentication);
    if (registrationId == null) {
      getRedirectStrategy().sendRedirect(request, response,
          mobileRedirectUrl + "?error=unknown_provider");
      return;
    }

    String providerId;
    String email;
    String name;

    if ("google".equals(registrationId)) {
      providerId = (String) attributes.get("sub");
      email = (String) attributes.get("email");
      name = (String) attributes.get("name");
    } else {
      getRedirectStrategy().sendRedirect(request, response,
          mobileRedirectUrl + "?error=unsupported_provider");
      return;
    }

    AuthResponse authResponse = oAuth2Service.findOrCreateAndIssueTokens(providerId, email, name,
        registrationId);
    String baseRedirectUrl = mobileRedirectUrl;

    String redirectUrl = UriComponentsBuilder.fromUriString(baseRedirectUrl)
        .queryParam("token", authResponse.getToken())
        .queryParam("refreshToken", authResponse.getRefreshToken())
        .queryParam("userId", authResponse.getId())
        .queryParam("username",
            authResponse.getUsername() != null ? authResponse.getUsername() : "")
        .queryParam("email", authResponse.getEmail() != null ? authResponse.getEmail() : "")
        .queryParam("fullName",
            authResponse.getFullName() != null ? authResponse.getFullName() : "")
        .queryParam("role", authResponse.getRole() != null ? authResponse.getRole() : "")
        .build()
        .toUriString();

    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  private String getRegistrationId(Authentication authentication) {
    if (authentication instanceof OAuth2AuthenticationToken token) {
      return token.getAuthorizedClientRegistrationId();
    }
    return null;
  }
}