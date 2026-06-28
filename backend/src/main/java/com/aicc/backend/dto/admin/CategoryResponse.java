package com.aicc.backend.dto.admin;

import com.aicc.backend.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        String description
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }
}
