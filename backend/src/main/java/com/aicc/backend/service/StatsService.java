package com.aicc.backend.service;

import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.stats.CategoryStatResponse;
import com.aicc.backend.dto.stats.DailyStatResponse;
import com.aicc.backend.dto.stats.SummaryResponse;
import com.aicc.backend.repository.ConsultationRepository;
import com.aicc.backend.repository.DailyStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatsService {

    private final DailyStatsRepository dailyStatsRepository;
    private final ConsultationRepository consultationRepository;

    public StatsService(DailyStatsRepository dailyStatsRepository,
                        ConsultationRepository consultationRepository) {
        this.dailyStatsRepository = dailyStatsRepository;
        this.consultationRepository = consultationRepository;
    }

    @Transactional(readOnly = true)
    public List<DailyStatResponse> daily(LocalDate from, LocalDate to) {
        return dailyStatsRepository.findByStatDateBetweenOrderByStatDateAsc(from, to).stream()
                .map(DailyStatResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryStatResponse> category() {
        return consultationRepository.countByCategory().stream()
                .map(c -> new CategoryStatResponse(c.getName() != null ? c.getName() : "미분류", c.getCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        long resolved = consultationRepository.countByStatus(ConsultationStatus.RESOLVED);
        long unresolved = consultationRepository.countByStatus(ConsultationStatus.UNRESOLVED);
        long inProgress = consultationRepository.countByStatus(ConsultationStatus.IN_PROGRESS);
        long total = resolved + unresolved + inProgress;
        double resolutionRate = total == 0 ? 0.0 : Math.round((resolved * 10000.0 / total)) / 100.0;
        return new SummaryResponse(total, resolved, unresolved, inProgress, resolutionRate);
    }
}
