package com.aicc.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * 상담 이력이 생기거나 상태가 바뀌면 그 날짜의 일별 통계만 바로 다시 집계한다.
 * 새벽 배치(DailyStatsTasklet)와 같은 집계이고, 배치를 기다리지 않아도 대시보드가 최신을 보여 준다.
 * 통계 갱신이 실패해도 상담 흐름은 막지 않는다.
 */
@Component
public class DailyStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(DailyStatsRefresher.class);

    private final JdbcTemplate jdbcTemplate;

    public DailyStatsRefresher(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void refresh(LocalDate date) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO daily_stats (stat_date, total_count, resolved_count, unresolved_count, in_progress_count)
                    SELECT ?, COUNT(*),
                           COALESCE(SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END), 0),
                           COALESCE(SUM(CASE WHEN status = 'UNRESOLVED' THEN 1 ELSE 0 END), 0),
                           COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0)
                    FROM consultation
                    WHERE created_at >= ? AND created_at < ?
                    ON DUPLICATE KEY UPDATE
                           total_count = VALUES(total_count),
                           resolved_count = VALUES(resolved_count),
                           unresolved_count = VALUES(unresolved_count),
                           in_progress_count = VALUES(in_progress_count)
                    """,
                    java.sql.Date.valueOf(date),
                    Timestamp.valueOf(date.atStartOfDay()),
                    Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
        } catch (Exception e) {
            log.warn("{} 일별 통계 갱신 실패: {}", date, e.getMessage());
        }
    }
}
