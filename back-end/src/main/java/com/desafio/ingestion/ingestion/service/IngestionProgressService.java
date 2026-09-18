package com.desafio.ingestion.ingestion.service;

import com.desafio.ingestion.ingestion.entity.JobStatus;
import com.desafio.ingestion.ingestion.repository.IngestionJobRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionProgressService {
  private final IngestionJobRepository jobs;

  public IngestionProgressService(IngestionJobRepository jobs) {
    this.jobs = jobs;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void update(UUID jobId, long processed, long valid, long invalid) {
    jobs.updateProgress(jobId, JobStatus.PROCESSING, processed, valid, invalid);
  }
}
