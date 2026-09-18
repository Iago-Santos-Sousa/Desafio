package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.time.Instant;
import java.util.UUID;

public record IngestionJobListItem(
    UUID jobId,
    String originalFilename,
    long fileSizeBytes,
    JobStatus status,
    Instant createdAt,
    Instant updatedAt) {}
