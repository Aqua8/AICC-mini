package com.aicc.backend.controller;

import com.aicc.backend.dto.stats.CategoryStatResponse;
import com.aicc.backend.dto.stats.DailyStatResponse;
import com.aicc.backend.dto.stats.SummaryResponse;
import com.aicc.backend.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyStatResponse>> daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(30);
        return ResponseEntity.ok(statsService.daily(start, end));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryStatResponse>> category() {
        return ResponseEntity.ok(statsService.category());
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> summary() {
        return ResponseEntity.ok(statsService.summary());
    }
}
