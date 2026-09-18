package com.desafio.ingestion.ingestion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class CsvHeaderValidator {
  private static final String EXPECTED_HEADER = "occurred_at,category,amount,description";

  public void validate(Path file) throws IOException {
    String header;

    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      header = reader.readLine();
    }

    if (header == null || !EXPECTED_HEADER.equals(stripBom(header).trim())) {
      throw new CsvFormatException(
          "CSV_HEADER_INVALID", "CSV deve usar cabeçalho: " + EXPECTED_HEADER);
    }
  }

  private String stripBom(String value) {
    return value.startsWith("\uFEFF") ? value.substring(1) : value;
  }
}
