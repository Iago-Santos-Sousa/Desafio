package com.desafio.ingestion.ingestion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRow(
    UUID jobId, Instant occurredAt, String category, BigDecimal amount, String description) {}
