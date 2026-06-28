package com.aicc.backend.dto.auth;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String name,
        String role
) {
}
