package com.aicc.backend.dto.consultation;

/** categoryId가 null이면 분류를 비운다(미분류). */
public record ConsultationCategoryRequest(Long categoryId) {
}
