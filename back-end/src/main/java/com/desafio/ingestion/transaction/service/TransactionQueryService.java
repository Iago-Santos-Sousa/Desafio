package com.desafio.ingestion.transaction.service;

import com.desafio.ingestion.transaction.dto.CategoryPageResponse;
import com.desafio.ingestion.transaction.dto.TransactionDto;
import com.desafio.ingestion.transaction.dto.TransactionPageResponse;
import com.desafio.ingestion.transaction.repository.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionQueryService {
  private final TransactionRepository repository;

  public TransactionQueryService(TransactionRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public TransactionPageResponse list(int requestedSize, Long cursor, UUID jobId, String category) {
    int limit = Math.min(Math.max(requestedSize, 1), 200);
    List<TransactionDto> rows = repository.findPage(limit, cursor, jobId, category);

    boolean hasMore = rows.size() > limit;

    if (hasMore) {
      rows = rows.subList(0, limit);
    }

    Long nextCursor = hasMore && !rows.isEmpty() ? rows.get(rows.size() - 1).id() : null;
    return new TransactionPageResponse(rows, nextCursor);
  }

  @Transactional(readOnly = true)
  public CategoryPageResponse categories(
      int requestedSize, UUID jobId, String search, String cursor) {
    int limit = Math.min(Math.max(requestedSize, 1), 20);

    List<String> rows = repository.findCategories(limit, jobId, search, cursor);

    boolean hasMore = rows.size() > limit;

    if (hasMore) {
      rows = rows.subList(0, limit);
    }

    String nextCursor = hasMore && !rows.isEmpty() ? rows.get(rows.size() - 1) : null;
    
    return new CategoryPageResponse(rows, nextCursor);
  }
}
