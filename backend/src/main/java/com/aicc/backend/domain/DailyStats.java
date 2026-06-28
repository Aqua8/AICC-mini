package com.aicc.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_stats", uniqueConstraints = @UniqueConstraint(columnNames = "stat_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(nullable = false)
    private long totalCount;

    @Column(nullable = false)
    private long resolvedCount;

    @Column(nullable = false)
    private long unresolvedCount;

    @Column(nullable = false)
    private long inProgressCount;

    @Builder
    public DailyStats(LocalDate statDate, long totalCount, long resolvedCount, long unresolvedCount, long inProgressCount) {
        this.statDate = statDate;
        this.totalCount = totalCount;
        this.resolvedCount = resolvedCount;
        this.unresolvedCount = unresolvedCount;
        this.inProgressCount = inProgressCount;
    }
}
