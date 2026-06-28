package com.aicc.backend.dto.stats;

import com.aicc.backend.domain.DailyStats;

import java.time.LocalDate;

public record DailyStatResponse(
        LocalDate date,
        long totalCount,
        long resolvedCount,
        long unresolvedCount,
        long inProgressCount
) {
    public static DailyStatResponse from(DailyStats s) {
        return new DailyStatResponse(s.getStatDate(), s.getTotalCount(),
                s.getResolvedCount(), s.getUnresolvedCount(), s.getInProgressCount());
    }
}
