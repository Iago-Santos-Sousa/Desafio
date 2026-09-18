package com.desafio.ingestion.ingestion.dto;

import java.util.List;

public record IngestionJobPageResponse(List<IngestionJobListItem> items, String nextCursor) {}
