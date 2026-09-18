package com.desafio.ingestion.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_job")
public class IngestionJob {
  @Id private UUID id;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false)
  private String storedPath;

  private long fileSizeBytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JobStatus status;

  private long totalRows;
  private long processedRows;
  private long validRows;
  private long invalidRows;

  @Column(length = 1000)
  private String errorSummary;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant queuedAt;
  private Instant startedAt;
  private Instant finishedAt;
  private Instant updatedAt;

  protected IngestionJob() {}

  public IngestionJob(UUID id, String originalFilename, String storedPath, long fileSizeBytes) {
    this.id = id;
    this.originalFilename = originalFilename;
    this.storedPath = storedPath;
    this.fileSizeBytes = fileSizeBytes;
    this.status = JobStatus.RECEIVED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public String getStoredPath() {
    return storedPath;
  }

  public long getFileSizeBytes() {
    return fileSizeBytes;
  }

  public JobStatus getStatus() {
    return status;
  }

  public long getTotalRows() {
    return totalRows;
  }

  public long getProcessedRows() {
    return processedRows;
  }

  public long getValidRows() {
    return validRows;
  }

  public long getInvalidRows() {
    return invalidRows;
  }

  public String getErrorSummary() {
    return errorSummary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getQueuedAt() {
    return queuedAt;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void queued() {
    status = JobStatus.QUEUED;
    queuedAt = Instant.now();
    updatedAt = Instant.now();
  }

  public void processing() {
    status = JobStatus.PROCESSING;
    startedAt = Instant.now();
    updatedAt = Instant.now();
  }

  public void finish(
      JobStatus finalStatus, long processed, long valid, long invalid, String error) {
    status = finalStatus;
    totalRows = processed;
    processedRows = processed;
    validRows = valid;
    invalidRows = invalid;
    errorSummary = error;
    finishedAt = Instant.now();
    updatedAt = Instant.now();
  }
}
