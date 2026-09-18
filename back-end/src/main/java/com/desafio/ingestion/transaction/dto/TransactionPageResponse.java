package com.desafio.ingestion.transaction.dto;

import java.util.List;

public record TransactionPageResponse(List<TransactionDto> items, Long nextCursor) {}
