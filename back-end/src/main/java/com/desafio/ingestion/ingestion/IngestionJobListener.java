package com.desafio.ingestion.ingestion;

import com.desafio.ingestion.analytics.service.AnalyticsService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IngestionJobListener implements JobExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionJobListener.class);
  private final IngestionJobRepository jobs;
  private final AnalyticsService analytics;

  public IngestionJobListener(IngestionJobRepository jobs, AnalyticsService analytics) {
    this.jobs = jobs;
    this.analytics = analytics;
  }

  @Override
  @Transactional
  public void beforeJob(JobExecution e) {
    jobs.findById(UUID.fromString(e.getJobParameters().getString("jobId")))
        .ifPresent(
            j -> {
              j.processing();
              jobs.save(j);
            });
  }

  @Override
  @Transactional
  public void afterJob(JobExecution e) {
    UUID id = UUID.fromString(e.getJobParameters().getString("jobId"));
    long valid = e.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
    long invalid = e.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();
    long processed = valid + invalid;

    JobStatus s =
        e.getStatus() == BatchStatus.COMPLETED
            ? (invalid > 0 ? JobStatus.COMPLETED_WITH_ERRORS : JobStatus.COMPLETED)
            : JobStatus.FAILED;

    String error = s == JobStatus.FAILED ? "Batch processing failed" : null;

    if (s == JobStatus.COMPLETED || s == JobStatus.COMPLETED_WITH_ERRORS) {
      try {
        analytics.refreshAggregates(id);
      } catch (RuntimeException ex) {
        LOGGER.error("Aggregate refresh failed for jobId={}", id, ex);
        s = JobStatus.FAILED;
        error = "Aggregate refresh failed";
      }
    }

    if (s == JobStatus.FAILED) {
      e.getAllFailureExceptions().forEach(ex -> LOGGER.error("Batch failed for jobId={}", id, ex));
    }

    JobStatus finalStatus = s;
    String finalError = error;
    
    jobs.findById(id)
        .ifPresent(
            j -> {
              j.finish(finalStatus, processed, valid, invalid, finalError);
              jobs.save(j);
            });
  }
}
