package com.aicc.backend.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyStatsTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public DailyStatsTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String sql = """
                INSERT INTO daily_stats (stat_date, total_count, resolved_count, unresolved_count, in_progress_count)
                SELECT DATE(created_at) AS stat_date,
                       COUNT(*) AS total_count,
                       SUM(status = 'RESOLVED') AS resolved_count,
                       SUM(status = 'UNRESOLVED') AS unresolved_count,
                       SUM(status = 'IN_PROGRESS') AS in_progress_count
                FROM consultation
                GROUP BY DATE(created_at)
                ON DUPLICATE KEY UPDATE
                       total_count = VALUES(total_count),
                       resolved_count = VALUES(resolved_count),
                       unresolved_count = VALUES(unresolved_count),
                       in_progress_count = VALUES(in_progress_count)
                """;
        int affected = jdbcTemplate.update(sql);
        log.info("Daily stats aggregation complete. Rows affected: {}", affected);
        return RepeatStatus.FINISHED;
    }
}
