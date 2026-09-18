package com.desafio.ingestion.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AggregateDto(
    LocalDate month, String category, BigDecimal totalAmount, long transactionCount) {}
