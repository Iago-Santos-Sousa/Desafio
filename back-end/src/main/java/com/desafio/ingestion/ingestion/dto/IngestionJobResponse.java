package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.time.Instant;
import java.util.UUID;

public record IngestionJobResponse(
    UUID jobId,
    String originalFilename,
    JobStatus status,
    long totalRows,
    long processedRows,
    long validRows,
    long invalidRows,
    long fileSizeBytes,
    Instant createdAt,
    Instant queuedAt,
    Instant startedAt,
    Instant finishedAt,
    Instant updatedAt,
    String errorSummary) {
  public static IngestionJobResponse from(IngestionJob job) {
    return new IngestionJobResponse(
        job.getId(),
        job.getOriginalFilename(),
        job.getStatus(),
        job.getTotalRows(),
        job.getProcessedRows(),
        job.getValidRows(),
        job.getInvalidRows(),
        job.getFileSizeBytes(),
        job.getCreatedAt(),
        job.getQueuedAt(),
        job.getStartedAt(),
        job.getFinishedAt(),
        job.getUpdatedAt(),
        job.getErrorSummary());
  }
}
