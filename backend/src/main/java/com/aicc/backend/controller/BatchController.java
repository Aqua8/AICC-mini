package com.aicc.backend.controller;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/batch")
public class BatchController {

    private final JobOperator jobOperator;
    private final Job dataGenerationJob;
    private final Job dailyStatsJob;

    public BatchController(JobOperator jobOperator, Job dataGenerationJob, Job dailyStatsJob) {
        this.jobOperator = jobOperator;
        this.dataGenerationJob = dataGenerationJob;
        this.dailyStatsJob = dailyStatsJob;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate() throws Exception {
        return ResponseEntity.ok(run(dataGenerationJob));
    }

    @PostMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() throws Exception {
        return ResponseEntity.ok(run(dailyStatsJob));
    }

    private Map<String, Object> run(Job job) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        var execution = jobOperator.start(job, params);
        return Map.of(
                "job", job.getName(),
                "executionId", execution.getId(),
                "status", execution.getStatus().toString());
    }
}
