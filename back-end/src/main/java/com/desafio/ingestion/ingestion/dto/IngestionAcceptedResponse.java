package com.desafio.ingestion.ingestion.dto;

import com.desafio.ingestion.ingestion.entity.IngestionJob;
import com.desafio.ingestion.ingestion.entity.JobStatus;
import java.util.UUID;

public record IngestionAcceptedResponse(UUID jobId, JobStatus status, String statusUrl) {
  public static IngestionAcceptedResponse from(IngestionJob job) {
    return new IngestionAcceptedResponse(
        job.getId(), job.getStatus(), "/api/v1/ingestions/" + job.getId());
  }
}
