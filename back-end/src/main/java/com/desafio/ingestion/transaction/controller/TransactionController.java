package com.desafio.ingestion.transaction.controller;

import com.desafio.ingestion.transaction.dto.CategoryPageResponse;
import com.desafio.ingestion.transaction.dto.TransactionPageResponse;
import com.desafio.ingestion.transaction.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions")
public class TransactionController {
  private final TransactionQueryService service;

  public TransactionController(TransactionQueryService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(summary = "List transactions with keyset pagination")
  public TransactionPageResponse list(
      @RequestParam(defaultValue = "50") int size,
      @RequestParam(required = false) Long cursor,
      @RequestParam(required = false) UUID jobId,
      @RequestParam(required = false) String category) {
    return service.list(size, cursor, jobId, category);
  }

  @GetMapping("/categories")
  @Operation(summary = "List categories for a job")
  public CategoryPageResponse categories(
      @RequestParam UUID jobId,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String cursor) {
    return service.categories(size, jobId, search, cursor);
  }
}
