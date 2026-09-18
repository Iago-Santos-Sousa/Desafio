package com.desafio.ingestion.analytics.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyAggregateProjection {
  LocalDate getMonth();

  String getCategory();

  BigDecimal getTotalAmount();

  Long getTransactionCount();
}
