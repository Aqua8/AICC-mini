package com.aicc.backend.repository;

import com.aicc.backend.domain.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    List<DailyStats> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);
    Optional<DailyStats> findByStatDate(LocalDate statDate);
}
