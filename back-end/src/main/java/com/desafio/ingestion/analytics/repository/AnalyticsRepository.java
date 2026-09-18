package com.desafio.ingestion.analytics.repository;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class AnalyticsRepository {
  private final JdbcTemplate jdbc;
  private final String businessTimeZone;

  public AnalyticsRepository(
    JdbcTemplate jdbc,
    @Value("${app.business-time-zone:America/Sao_Paulo}") String businessTimeZone) {
    this.jdbc = jdbc;
    this.businessTimeZone = businessTimeZone;
  }

  public SummaryDto findSummary(LocalDate from, LocalDate to) {
    return jdbc.queryForObject(
      "SELECT coalesce(sum(transaction_count), 0), coalesce(sum(total_amount), 0), count(distinct"
        + " category) FROM daily_job_category_aggregate WHERE day BETWEEN ? AND ?",
      new Object[]{from, to},
      (rs, rowNum) -> new SummaryDto(rs.getLong(1), rs.getBigDecimal(2), rs.getLong(3)));
  }

  public List<AggregateDto> findMonthlyAggregates(LocalDate from, LocalDate to) {
    return jdbc.query(
      "SELECT date_trunc('month', day)::date, category, sum(total_amount), sum(transaction_count)"
        + " FROM daily_job_category_aggregate WHERE day BETWEEN ? AND ? GROUP BY 1, category"
        + " ORDER BY 1, category",
      new Object[]{from, to},
      (rs, rowNum) ->
        new AggregateDto(
          rs.getDate(1).toLocalDate(), rs.getString(2), rs.getBigDecimal(3), rs.getLong(4)));
  }

  public void refreshAggregates(UUID jobId) {
    jdbc.update("DELETE FROM daily_job_category_aggregate WHERE ingestion_job_id = ?", jobId);
    jdbc.update(
      "INSERT INTO daily_job_category_aggregate(ingestion_job_id, day, category, total_amount,"
        + " transaction_count) SELECT ingestion_job_id, (occurred_at AT TIME ZONE ?)::date,"
        + " category, sum(amount), count(*) FROM transaction_record WHERE ingestion_job_id = ?"
        + " GROUP BY ingestion_job_id, 2, category",
      businessTimeZone,
      jobId);
  }
}
