package com.example.BTL_Mobile.config;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Value("${oauth2.mobile-redirect-url:elearn://login/callback}")
  private String mobileRedirectUrl;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      AuthenticationProvider authenticationProvider
  ) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    RequestMatcher apiMatcher = request -> {
      String uri = request.getRequestURI();
      return uri != null && uri.startsWith("/api/");
    };

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
            .requestMatchers(
                "/api/auth/register",
                "/api/auth/register/verify",
                "/api/auth/register/complete",
                "/api/auth/login",
                "/api/auth/oauth2/google",
                "/api/auth/forgot-password",
                "/api/auth/forgot-password/verify",
                "/api/auth/reset-password",
                "/api/auth/password-strength",
                "/api/auth/oauth2/authorize/google",
                "/api/auth/refresh",
                "/api/auth/refresh/",
                "/api/auth/logout",
                "/api/auth/test",
                "/api/auth/validate",
                "/api/topics/*/lessons",
                "/api/topic/*/vocabularies",
                "/api/scenarios/by-lesson/*",
                "/api/ai/respond",
                "/api/conversation/improve",
                "/conversation/improve"
            ).permitAll()
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .requestMatchers("/api/auth/**").authenticated()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .exceptionHandling(ex -> ex
            .defaultAuthenticationEntryPointFor(
                (request, response, authException) -> {
                  response.setStatus(HttpStatus.UNAUTHORIZED.value());
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                  ApiResponse<Object> body = ApiResponse.error(
                      "Bạn cần đăng nhập để truy cập tài nguyên này",
                      "UNAUTHORIZED"
                  );
                  objectMapper.writeValue(response.getOutputStream(), body);
                },
                apiMatcher
            )
            .defaultAccessDeniedHandlerFor(
                (request, response, accessDeniedException) -> {
                  response.setStatus(HttpStatus.FORBIDDEN.value());
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                  ApiResponse<Object> body = ApiResponse.error(
                      "Bạn không có quyền truy cập tài nguyên này",
                      "FORBIDDEN"
                  );
                  objectMapper.writeValue(response.getOutputStream(), body);
                },
                apiMatcher
            )
        )
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2LoginSuccessHandler)
            .failureHandler((request, response, exception) -> {
              String redirectUrl = mobileRedirectUrl + "?error=oauth_failed&message=" +
                  java.net.URLEncoder.encode(exception.getMessage(),
                      java.nio.charset.StandardCharsets.UTF_8);
              response.sendRedirect(redirectUrl);
            })
        )
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of("*")); // Cho phép tất cả origins (có thể giới hạn cho production)
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
