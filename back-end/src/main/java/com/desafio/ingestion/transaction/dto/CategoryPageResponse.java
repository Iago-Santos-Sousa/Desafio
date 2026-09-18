package com.desafio.ingestion.transaction.dto;

import java.util.List;

public record CategoryPageResponse(List<String> items, String nextCursor) {}
