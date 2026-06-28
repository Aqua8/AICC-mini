package com.aicc.backend.dto.admin;

import com.aicc.backend.domain.Faq;

public record FaqResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String question,
        String answer
) {
    public static FaqResponse from(Faq f) {
        return new FaqResponse(
                f.getId(),
                f.getCategory() != null ? f.getCategory().getId() : null,
                f.getCategory() != null ? f.getCategory().getName() : null,
                f.getQuestion(),
                f.getAnswer());
    }
}
