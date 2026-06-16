package com.medibook.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.medibook.security.model.CustomUserPrincipal;

public final class SecurityUtils {

    public SecurityUtils() {
    }

    public static Long getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        return principal.getUserId();
    }

    public static String getCurrentEmail() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        return principal.getEmail();
    }
}
