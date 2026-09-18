package com.desafio.ingestion.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionService {
  private final IngestionJobRepository jobs;
  private final RabbitTemplate rabbit;
  private final Path uploadDir;
  private final CsvHeaderValidator headerValidator;

  public IngestionService(
      IngestionJobRepository jobs,
      RabbitTemplate rabbit,
      CsvHeaderValidator headerValidator,
      @Value("${app.upload-dir:/data/uploads}") String dir) {
    this.jobs = jobs;
    this.rabbit = rabbit;
    this.headerValidator = headerValidator;
    this.uploadDir = Path.of(dir);
  }

  @Transactional
  public IngestionJob accept(MultipartFile file) throws IOException {
    if (file.isEmpty()
        || file.getOriginalFilename() == null
        || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("CSV file is required");
    }

    Files.createDirectories(uploadDir);
    UUID id = UUID.randomUUID();
    Path target = uploadDir.resolve(id + ".csv");

    try {
      try (InputStream in = file.getInputStream();
          OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
        in.transferTo(out);
      }

      headerValidator.validate(target);
      IngestionJob job =
          jobs.save(
              new IngestionJob(
                  id, file.getOriginalFilename(), target.toString(), Files.size(target)));

      job.queued();
      jobs.save(job);

      rabbit.convertAndSend("ingestion.exchange", "ingestion.jobs", new JobMessage(id));

      return job;
    } catch (IOException | RuntimeException ex) {
      try {
        Files.deleteIfExists(target);
      } catch (IOException cleanup) {
        ex.addSuppressed(cleanup);
      }

      throw ex;
    }
  }

  @Transactional(readOnly = true)
  public java.util.List<IngestionJob> active(int requestedLimit) {
    int limit = Math.min(Math.max(requestedLimit, 1), 50);

    return jobs.findByStatusInOrderByCreatedAtAsc(
        java.util.List.of(JobStatus.RECEIVED, JobStatus.QUEUED, JobStatus.PROCESSING),
        org.springframework.data.domain.PageRequest.of(0, limit));
  }

  @Transactional(readOnly = true)
  public IngestionJob find(UUID id) {
    return jobs.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Ingestion job not found"));
  }
}
