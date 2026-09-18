package com.desafio.ingestion.ingestion.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
public class JobMessageListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobMessageListener.class);
  private final JobLauncher launcher;
  private final Job job;

  public JobMessageListener(JobLauncher launcher, Job job) {
    this.launcher = launcher;
    this.job = job;
  }

  @RabbitListener(queues = "ingestion.jobs")
  public void consume(JobMessage message) throws Exception {
    LOGGER.info("event=job_received jobId={}", message.jobId());

    try {
      launcher.run(
          job,
          new JobParametersBuilder()
              .addString("jobId", message.jobId().toString())
              .addString(
                  "filePath",
                  java.nio.file.Path.of("/data/uploads", message.jobId() + ".csv").toString())
              .addLong("run", System.currentTimeMillis())
              .toJobParameters());
    } catch (Exception exception) {
      LOGGER.error("event=job_processing_error jobId={}", message.jobId(), exception);
      throw exception;
    }
  }
}
