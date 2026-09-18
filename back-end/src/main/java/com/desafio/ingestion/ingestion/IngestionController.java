package com.desafio.ingestion.ingestion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingestions")
@Tag(name = "Ingestions")
public class IngestionController {
  private final IngestionService service;
  private final IngestionQueryService queryService;

  public IngestionController(IngestionService service, IngestionQueryService queryService) {
    this.service = service;
    this.queryService = queryService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Upload CSV for asynchronous ingestion")
  public Map<String, Object> upload(@RequestPart("file") MultipartFile file) throws IOException {
    IngestionJob j = service.accept(file);
    return Map.of(
        "jobId",
        j.getId(),
        "status",
        j.getStatus(),
        "statusUrl",
        "/api/v1/ingestions/" + j.getId());
  }

  @GetMapping
  public IngestionJobPageResponse list(
      @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String cursor) {
    return queryService.list(size, cursor);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Read ingestion progress")
  public IngestionJobResponse status(@PathVariable UUID id) {
    return IngestionJobResponse.from(service.find(id));
  }

  @GetMapping("/active")
  @Operation(summary = "List active ingestions")
  public java.util.List<IngestionJobResponse> active(@RequestParam(defaultValue = "10") int limit) {
    return service.active(limit).stream().map(IngestionJobResponse::from).toList();
  }
}
