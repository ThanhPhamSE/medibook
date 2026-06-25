package com.medibook.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.UnauthorizedException;
import com.medibook.security.model.CustomUserPrincipal;

@Component
public final class SecurityUtils {

    public SecurityUtils() {
    }

    public Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserPrincipal user) {
            return user.getUserId();
        }

        throw new UnauthorizedException("Invalid authentication principal");
    }

    public static String getCurrentEmail() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserPrincipal user) {
            return user.getEmail();
        }

        throw new UnauthorizedException("Invalid authentication principal");
    }
}
