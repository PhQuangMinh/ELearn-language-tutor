package com.example.BTL_Mobile.security;

import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Kiểm tra token có bị blacklist không (đã logout)
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    // Token đã bị revoke, không cho phép truy cập
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                // Validate token structure và expiration trước
                if (jwtTokenProvider.isValidToken(jwt)) {
                    String username = jwtTokenProvider.extractUsername(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                        // Nếu user đã logout sau thời điểm token được cấp -> token bị vô hiệu
                        if (userDetails instanceof User user) {
                            LocalDateTime lastLogoutAt = user.getLastLogoutAt();
                            if (lastLogoutAt != null) {
                                Date issuedAt = jwtTokenProvider.extractIssuedAt(jwt);
                                if (issuedAt != null) {
                                    boolean issuedBeforeLogout = issuedAt.toInstant().isBefore(
                                            lastLogoutAt.atZone(ZoneId.systemDefault()).toInstant()
                                    );
                                    if (issuedBeforeLogout) {
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                        return;
                                    }
                                }
                            }
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
