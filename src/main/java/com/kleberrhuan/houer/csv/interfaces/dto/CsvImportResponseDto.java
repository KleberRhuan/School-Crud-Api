/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.dto;

import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import java.time.Instant;
import java.util.UUID;

public record CsvImportResponseDto(
  UUID jobId,
  String filename,
  ImportJobStatus status,
  Integer totalRecords,
  Integer processedRecords,
  Integer errorRecords,
  String errorMessage,
  Instant startedAt,
  Instant finishedAt,
  Instant createdAt
) {}
