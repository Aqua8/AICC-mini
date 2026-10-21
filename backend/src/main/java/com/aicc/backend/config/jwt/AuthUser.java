package com.aicc.backend.config.jwt;

import org.springframework.security.core.Authentication;

/** JwtAuthenticationFilter와 STOMP 인터셉터가 만든 Authentication에서 사용자 정보를 꺼낸다. */
public final class AuthUser {

    private AuthUser() {
    }

    public static Long userId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof Long id ? id : null;
    }

    public static boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
