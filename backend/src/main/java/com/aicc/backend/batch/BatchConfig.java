package com.aicc.backend.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    public static final String DATA_GENERATION_JOB = "dataGenerationJob";
    public static final String DAILY_STATS_JOB = "dailyStatsJob";

    @Bean
    public Step dataGenerationStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                   DataGenerationTasklet tasklet) {
        return new StepBuilder("dataGenerationStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Job dataGenerationJob(JobRepository jobRepository, Step dataGenerationStep) {
        return new JobBuilder(DATA_GENERATION_JOB, jobRepository)
                .start(dataGenerationStep)
                .build();
    }

    @Bean
    public Step dailyStatsStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                               DailyStatsTasklet tasklet) {
        return new StepBuilder("dailyStatsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Job dailyStatsJob(JobRepository jobRepository, Step dailyStatsStep) {
        return new JobBuilder(DAILY_STATS_JOB, jobRepository)
                .start(dailyStatsStep)
                .build();
    }
}
