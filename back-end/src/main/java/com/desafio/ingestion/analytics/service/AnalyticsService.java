package com.desafio.ingestion.analytics.service;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.repository.AnalyticsRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
  private final AnalyticsRepository repository;
  private final DateRangeResolver dateRangeResolver;
  private final String businessTimeZone;

  public AnalyticsService(
      AnalyticsRepository repository,
      DateRangeResolver dateRangeResolver,
      @Value("${app.business-time-zone:America/Sao_Paulo}") String businessTimeZone) {
    this.repository = repository;
    this.dateRangeResolver = dateRangeResolver;
    this.businessTimeZone = businessTimeZone;
  }

  @Transactional(readOnly = true)
  public SummaryDto summary(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);

    return repository.findSummary(range.from(), range.to());
  }

  @Transactional(readOnly = true)
  public List<AggregateDto> monthlyAggregates(LocalDate from, LocalDate to) {
    DateRange range = dateRangeResolver.resolve(from, to);

    return repository.findMonthlyAggregates(range.from(), range.to()).stream()
        .map(
            row ->
                new AggregateDto(
                    row.getMonth(),
                    row.getCategory(),
                    row.getTotalAmount(),
                    row.getTransactionCount()))
        .toList();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void refreshAggregates(UUID jobId) {
    repository.refreshAggregates(jobId, businessTimeZone);
  }
}
