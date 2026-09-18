package com.desafio.ingestion.ingestion.batch;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {
  @Bean
  @StepScope
  FlatFileItemReader<TransactionRow> transactionReader(
      @Value("#{jobParameters['filePath']}") String filePath,
      @Value("#{jobParameters['jobId']}") String jobId) {
    var tokenizer = new DelimitedLineTokenizer();

    tokenizer.setNames("occurred_at", "category", "amount", "description");

    var mapper = new DefaultLineMapper<TransactionRow>();

    mapper.setLineTokenizer(tokenizer);

    mapper.setFieldSetMapper(
        (FieldSet fields) ->
            new TransactionRow(
                UUID.fromString(jobId),
                Instant.parse(fields.readString("occurred_at")),
                fields.readString("category").trim(),
                new BigDecimal(fields.readString("amount")),
                fields.readString("description")));
    mapper.afterPropertiesSet();

    var reader =
        new FlatFileItemReader<TransactionRow>(
            new org.springframework.core.io.FileSystemResource(Path.of(filePath)), mapper);

    reader.setLinesToSkip(1);
    reader.setStrict(true);

    return reader;
  }

  @Bean
  @StepScope
  JdbcBatchItemWriter<TransactionRow> transactionWriter(javax.sql.DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<TransactionRow>()
        .dataSource(dataSource)
        .sql(
            "INSERT INTO transaction_record"
                + " (ingestion_job_id,occurred_at,category,amount,description) VALUES (?,?,?,?,?)")
        .itemPreparedStatementSetter(
            (item, ps) -> {
              ps.setObject(1, item.jobId());
              ps.setTimestamp(2, java.sql.Timestamp.from(item.occurredAt()));
              ps.setString(3, item.category());
              ps.setBigDecimal(4, item.amount());
              ps.setString(5, item.description());
            })
        .build();
  }

  @Bean
  Step ingestionStep(
      JobRepository repo,
      PlatformTransactionManager tx,
      ItemReader<TransactionRow> reader,
      ItemWriter<TransactionRow> writer,
      IngestionJobListener listener,
      IngestionProgressChunkListener progressListener) {
    return new StepBuilder("ingestionStep", repo)
        .<TransactionRow, TransactionRow>chunk(2000, tx)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(10000)
        .listener(listener)
        .listener(progressListener)
        .build();
  }

  @Bean
  Job ingestionJob(JobRepository repo, Step ingestionStep, IngestionJobListener listener) {
    return new JobBuilder("ingestionJob", repo).listener(listener).start(ingestionStep).build();
  }
}
