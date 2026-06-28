package com.aicc.backend.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyStatsScheduler {

    private final JobOperator jobOperator;
    private final Job dailyStatsJob;

    public DailyStatsScheduler(JobOperator jobOperator, Job dailyStatsJob) {
        this.jobOperator = jobOperator;
        this.dailyStatsJob = dailyStatsJob;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void aggregateDailyStats() {
        try {
            jobOperator.start(dailyStatsJob, new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("Scheduled daily stats job failed", e);
        }
    }
}
