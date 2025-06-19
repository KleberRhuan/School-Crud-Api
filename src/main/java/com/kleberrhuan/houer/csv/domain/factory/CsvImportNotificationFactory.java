/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.factory;

import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CsvImportNotificationFactory {

  public static CsvImportNotification of(
    CsvImportJob job,
    ImportJobStatus status,
    String message
  ) {
    return new CsvImportNotification(
      job.getId(),
      job.getCreatedBy(),
      status,
      job.getFilename(),
      job.getTotalRecords(),
      job.getProcessedRecords(),
      job.getErrorRecords(),
      message,
      "info",
      Instant.now()
    );
  }

  public static CsvImportNotification progress(CsvImportJob job) {
    String msg =
      "Processados %d/%d registros – erros: %d".formatted(
          job.getProcessedRecords(),
          job.getTotalRecords(),
          job.getErrorRecords()
        );
    return of(job, ImportJobStatus.RUNNING, msg);
  }
}
