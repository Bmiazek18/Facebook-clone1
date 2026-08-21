package com.facebook.analytics.repository;

import com.facebook.analytics.model.PageDailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageDailyMetricRepository extends JpaRepository<PageDailyMetric, UUID> {
    Optional<PageDailyMetric> findByPageIdAndMetricDate(UUID pageId, LocalDate metricDate);
    List<PageDailyMetric> findByPageIdAndMetricDateBetweenOrderByMetricDateAsc(UUID pageId, LocalDate startDate, LocalDate endDate);
    List<PageDailyMetric> findByPageIdOrderByMetricDateDesc(UUID pageId);
}
