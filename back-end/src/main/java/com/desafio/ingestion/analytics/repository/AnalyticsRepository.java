package com.desafio.ingestion.analytics.repository;

import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.entity.DailyJobCategoryAggregate;
import com.desafio.ingestion.analytics.entity.DailyJobCategoryAggregateId;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository
    extends Repository<DailyJobCategoryAggregate, DailyJobCategoryAggregateId> {
  @Query(
      "select new com.desafio.ingestion.analytics.dto.SummaryDto("
          + "coalesce(sum(a.transactionCount), 0), "
          + "coalesce(sum(a.totalAmount), 0), "
          + "count(distinct a.id.category)) "
          + "from DailyJobCategoryAggregate a "
          + "where a.id.day between :from and :to")
  SummaryDto findSummary(@Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query(
      value =
          "select date_trunc('month', day)::date as \"month\", category as \"category\", "
              + "sum(total_amount) as \"totalAmount\", "
              + "sum(transaction_count) as \"transactionCount\" "
              + "from daily_job_category_aggregate "
              + "where day between :from and :to "
              + "group by 1, category order by 1, category",
      nativeQuery = true)
  List<MonthlyAggregateProjection> findMonthlyAggregates(
      @Param("from") LocalDate from, @Param("to") LocalDate to);

  @Modifying
  @Query("delete from DailyJobCategoryAggregate a where a.id.ingestionJobId = :jobId")
  int deleteByJobId(@Param("jobId") UUID jobId);

  @Modifying
  @Query(
      value =
          "insert into daily_job_category_aggregate "
              + "(ingestion_job_id, day, category, total_amount, transaction_count) "
              + "select ingestion_job_id, (occurred_at at time zone :businessTimeZone)::date, "
              + "category, sum(amount), count(*) "
              + "from transaction_record where ingestion_job_id = :jobId "
              + "group by 1, 2, 3",
      nativeQuery = true)
  int insertByJobId(@Param("jobId") UUID jobId, @Param("businessTimeZone") String businessTimeZone);

  default void refreshAggregates(UUID jobId, String businessTimeZone) {
    deleteByJobId(jobId);
    insertByJobId(jobId, businessTimeZone);
  }
}
