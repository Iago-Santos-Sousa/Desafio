package com.desafio.ingestion.analytics.service;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.repository.AnalyticsRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private final AnalyticsRepository repository;
  private final DateRangeResolver dateRangeResolver;

  public AnalyticsService(AnalyticsRepository repository, DateRangeResolver dateRangeResolver) {
    this.repository = repository;
    this.dateRangeResolver = dateRangeResolver;
  }

  public SummaryDto summary(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);
    return repository.findSummary(range.from(), range.to());
  }

  public List<AggregateDto> monthlyAggregates(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);
    return repository.findMonthlyAggregates(range.from(), range.to());
  }

  public void refreshAggregates(UUID jobId) {
    repository.refreshAggregates(jobId);
  }
}
