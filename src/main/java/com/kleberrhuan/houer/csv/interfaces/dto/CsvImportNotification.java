/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.dto;

import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import java.time.Instant;
import java.util.UUID;

public record CsvImportNotification(
  UUID jobId,
  Long userId,
  ImportJobStatus status,
  String filename,
  int totalRecords,
  int processedRecords,
  int errorRecords,
  String message,
  String severity,
  Instant timestamp
) {
  public static CsvImportNotification info(
    UUID jobId,
    Long userId,
    ImportJobStatus status,
    String filename,
    int total,
    int processed,
    int errors,
    String message
  ) {
    return new CsvImportNotification(
      jobId,
      userId,
      status,
      filename,
      total,
      processed,
      errors,
      message,
      "info",
      Instant.now()
    );
  }

  public static CsvImportNotification error(
    UUID jobId,
    Long userId,
    ImportJobStatus status,
    String filename,
    int total,
    int processed,
    int errors,
    String errorMessage
  ) {
    return new CsvImportNotification(
      jobId,
      userId,
      status,
      filename,
      total,
      processed,
      errors,
      errorMessage,
      "error",
      Instant.now()
    );
  }

  public static CsvImportNotification success(
    UUID jobId,
    Long userId,
    ImportJobStatus status,
    String filename,
    int total,
    int processed,
    int errors,
    String message
  ) {
    return new CsvImportNotification(
      jobId,
      userId,
      status,
      filename,
      total,
      processed,
      errors,
      message,
      "success",
      Instant.now()
    );
  }
}
