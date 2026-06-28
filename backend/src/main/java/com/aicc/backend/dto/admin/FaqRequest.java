package com.aicc.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record FaqRequest(
        Long categoryId,
        @NotBlank String question,
        @NotBlank String answer
) {
}
