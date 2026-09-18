package com.desafio.ingestion.analytics.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DailyJobCategoryAggregateId implements Serializable {
  private UUID ingestionJobId;
  private LocalDate day;
  private String category;

  protected DailyJobCategoryAggregateId() {}

  public UUID getIngestionJobId() {
    return ingestionJobId;
  }

  public LocalDate getDay() {
    return day;
  }

  public String getCategory() {
    return category;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof DailyJobCategoryAggregateId that)) {
      return false;
    }
    
    return Objects.equals(ingestionJobId, that.ingestionJobId)
        && Objects.equals(day, that.day)
        && Objects.equals(category, that.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ingestionJobId, day, category);
  }
}
