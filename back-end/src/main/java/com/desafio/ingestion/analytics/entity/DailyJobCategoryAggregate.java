package com.desafio.ingestion.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "daily_job_category_aggregate")
public class DailyJobCategoryAggregate {
  @EmbeddedId private DailyJobCategoryAggregateId id;

  @Column(nullable = false, precision = 24, scale = 4)
  private BigDecimal totalAmount;

  @Column(nullable = false)
  private long transactionCount;

  protected DailyJobCategoryAggregate() {}

  public DailyJobCategoryAggregateId getId() {
    return id;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public long getTransactionCount() {
    return transactionCount;
  }
}
