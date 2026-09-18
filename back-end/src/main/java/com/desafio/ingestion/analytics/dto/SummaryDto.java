package com.desafio.ingestion.analytics.dto;

import java.math.BigDecimal;

public record SummaryDto(long transactionCount, BigDecimal totalAmount, long categoryCount) {}
