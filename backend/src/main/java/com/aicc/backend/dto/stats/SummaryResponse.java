package com.aicc.backend.dto.stats;

public record SummaryResponse(
        long totalConsultations,
        long resolved,
        long unresolved,
        long inProgress,
        double resolutionRate
) {
}
