package com.desafio.ingestion.analytics.controller;

import com.desafio.ingestion.analytics.dto.AggregateDto;
import com.desafio.ingestion.analytics.dto.SummaryDto;
import com.desafio.ingestion.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics")
public class AnalyticsController {
  private final AnalyticsService service;

  public AnalyticsController(AnalyticsService service) {
    this.service = service;
  }

  @GetMapping("/summary")
  @Operation(summary = "Dashboard totals")
  public SummaryDto summary(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    return service.summary(from, to);
  }

  @GetMapping("/monthly-by-category")
  @Operation(summary = "Monthly category aggregates")
  public List<AggregateDto> monthly(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    return service.monthlyAggregates(from, to);
  }
}
