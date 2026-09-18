package com.desafio.ingestion.ingestion.batch;

import com.desafio.ingestion.ingestion.service.IngestionProgressService;
import java.util.UUID;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class IngestionProgressChunkListener
    implements ChunkListener<TransactionRow, TransactionRow> {
  private final IngestionProgressService progress;
  private StepExecution stepExecution;

  public IngestionProgressChunkListener(IngestionProgressService progress) {
    this.progress = progress;
  }

  @BeforeStep
  public void captureStep(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }

  @Override
  public void beforeChunk(Chunk<TransactionRow> chunk) {}

  @Override
  public void afterChunk(Chunk<TransactionRow> chunk) {
    if (stepExecution == null) {
      return;
    }

    UUID jobId = UUID.fromString(stepExecution.getJobParameters().getString("jobId"));
    progress.update(
        jobId,
        stepExecution.getReadCount(),
        stepExecution.getWriteCount(),
        stepExecution.getSkipCount());
  }
}
