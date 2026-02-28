package com.example.BTL_Mobile.security;

import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserContext {

    /**
     * @return Optional userId of current authenticated user.
     */
    public Optional<Integer> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user && user.getId() != null) {
            return Optional.of(user.getId());
        }

        return Optional.empty();
    }

    /**
     * @return userId of current authenticated user, otherwise throw UNAUTHORIZED.
     */
    public Integer requireUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new BusinessException("Người dùng chưa đăng nhập!", "UNAUTHORIZED"));
    }
}

