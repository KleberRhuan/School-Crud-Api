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
    double percentage = calculatePercentage(
      job.getProcessedRecords(),
      job.getTotalRecords()
    );
    String progressBar = generateProgressBar(percentage);

    String msg = String.format(
      "Processando: %s (%.1f%%) – %d/%d registros processados – %d erros",
      progressBar,
      percentage,
      job.getProcessedRecords(),
      job.getTotalRecords(),
      job.getErrorRecords()
    );

    return of(job, ImportJobStatus.RUNNING, msg);
  }

  public static CsvImportNotification started(CsvImportJob job) {
    String msg = String.format(
      "🚀 Importação iniciada para arquivo '%s' com %d registros",
      job.getFilename(),
      job.getTotalRecords()
    );
    return of(job, ImportJobStatus.RUNNING, msg);
  }

  public static CsvImportNotification completed(CsvImportJob job) {
    String msg = String.format(
      "✅ Importação concluída! %d registros processados com %d erro(s)",
      job.getProcessedRecords(),
      job.getErrorRecords()
    );
    return of(job, ImportJobStatus.COMPLETED, msg);
  }

  public static CsvImportNotification failed(
    CsvImportJob job,
    String errorMessage
  ) {
    String msg = String.format(
      "❌ Importação falhou após processar %d/%d registros. Erro: %s",
      job.getProcessedRecords(),
      job.getTotalRecords(),
      errorMessage
    );
    return new CsvImportNotification(
      job.getId(),
      job.getCreatedBy(),
      ImportJobStatus.FAILED,
      job.getFilename(),
      job.getTotalRecords(),
      job.getProcessedRecords(),
      job.getErrorRecords(),
      msg,
      "error",
      Instant.now()
    );
  }

  private static double calculatePercentage(int processed, int total) {
    if (total == 0) return 0.0;
    return (processed * 100.0) / total;
  }

  private static String generateProgressBar(double percentage) {
    int filled = (int) (percentage / 10); // 10 chars total
    int empty = 10 - filled;

    StringBuilder bar = new StringBuilder();
    bar.append("█".repeat(Math.max(0, filled)));
    bar.append("░".repeat(Math.max(0, empty)));

    return bar.toString();
  }
}
