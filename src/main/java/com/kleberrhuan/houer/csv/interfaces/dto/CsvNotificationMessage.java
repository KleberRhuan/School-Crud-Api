/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.dto;

import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO para mensagens de notificação WebSocket sobre o progresso de importação
 * CSV.
 * 
 * Usado para comunicação em tempo real entre o backend e frontend durante
 * processamento de jobs de importação assíncrona.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvNotificationMessage {

  private UUID jobId;
  private Long userId;
  private ImportJobStatus status;
  private String filename;
  private Long totalRecords;
  private Long processedRecords;
  private Long errorRecords;
  private String errorMessage;
  private Instant timestamp;

  public Double getProgressPercentage() {
    if (totalRecords == null || totalRecords == 0) {
      return 0.0;
    }
    return (processedRecords.doubleValue() / totalRecords.doubleValue()) * 100.0;
  }

  public Boolean isFinished() {
    return status == ImportJobStatus.COMPLETED ||
        status == ImportJobStatus.FAILED;
  }
}
