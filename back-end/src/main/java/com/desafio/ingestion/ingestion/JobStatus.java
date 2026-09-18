package com.desafio.ingestion.ingestion;

public enum JobStatus {
  RECEIVED,
  QUEUED,
  PROCESSING,
  COMPLETED,
  COMPLETED_WITH_ERRORS,
  FAILED
}
