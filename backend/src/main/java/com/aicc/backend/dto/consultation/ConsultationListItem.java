package com.aicc.backend.dto.consultation;

import com.aicc.backend.domain.Consultation;
import com.aicc.backend.domain.ConsultationStatus;

import java.time.LocalDateTime;

public record ConsultationListItem(
        Long id,
        String title,
        String summary,
        String categoryName,
        ConsultationStatus status,
        LocalDateTime createdAt
) {
    public static ConsultationListItem from(Consultation c) {
        return new ConsultationListItem(
                c.getId(),
                c.getTitle(),
                c.getSummary(),
                c.getCategory() != null ? c.getCategory().getName() : null,
                c.getStatus(),
                c.getCreatedAt());
    }
}
