package com.desafio.ingestion.transaction.repository;

import com.desafio.ingestion.transaction.dto.TransactionDto;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryCustom {
  List<TransactionDto> findPage(int limit, Long cursor, UUID jobId, String category);

  List<String> findCategories(int limit, UUID jobId, String search, String cursor);
}
