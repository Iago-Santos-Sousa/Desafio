package com.desafio.ingestion.ingestion;

import java.util.List;

public record IngestionJobPageResponse(List<IngestionJobListItem> items, String nextCursor) {}
