package com.desafio.ingestion.transaction.entity;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaction_record")
public class TransactionRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ingestion_job_id", nullable = false)
  private IngestionJob ingestionJob;

  @Column(nullable = false)
  private Instant occurredAt;

  @Column(nullable = false, length = 120)
  private String category;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(length = 500)
  private String description;

  protected TransactionRecord() {}

  public Long getId() {
    return id;
  }

  public IngestionJob getIngestionJob() {
    return ingestionJob;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getCategory() {
    return category;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }
}
