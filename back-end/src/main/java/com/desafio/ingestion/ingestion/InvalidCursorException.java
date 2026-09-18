package com.desafio.ingestion.ingestion;

public class InvalidCursorException extends IllegalArgumentException {
  public InvalidCursorException(String message, Throwable cause) {
    super(message, cause);
  }
}
