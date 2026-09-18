package com.desafio.ingestion.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionDto(
    long id,
    UUID jobId,
    Instant occurredAt,
    String category,
    BigDecimal amount,
    String description) {}
