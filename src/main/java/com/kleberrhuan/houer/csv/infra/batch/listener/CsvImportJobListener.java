/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch.listener;

import static com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Jobs.FILENAME_PARAMETER_NAME;
import static com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Jobs.ID_PARAMETER_NAME;

import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvImportJobListener implements JobExecutionListener {

  private final CsvImportService importService;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    String jobIdStr = jobExecution
      .getJobParameters()
      .getString(ID_PARAMETER_NAME);
    String filename = jobExecution
      .getJobParameters()
      .getString(FILENAME_PARAMETER_NAME);

    if (jobIdStr != null) {
      UUID jobId = UUID.fromString(jobIdStr);

      MDC.put("jobId", jobId.toString());
      if (filename != null) {
        MDC.put("filename", filename);
      }

      log.info(
        "Iniciando job de importação {} para arquivo {}",
        jobId,
        filename
      );
      updateJobStatus(jobExecution, ImportJobStatus.RUNNING, null);
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String jobIdStr = jobExecution
      .getJobParameters()
      .getString(ID_PARAMETER_NAME);
    String filename = jobExecution
      .getJobParameters()
      .getString(FILENAME_PARAMETER_NAME);

    if (jobIdStr != null) {
      UUID jobId = UUID.fromString(jobIdStr);

      if (jobExecution.getStatus().isUnsuccessful()) {
        handleJobFailure(jobExecution, jobId, filename);
      } else {
        handleJobSuccess(jobExecution, jobId, filename);
      }
    }

    MDC.clear();
  }

  private void handleJobFailure(
    JobExecution jobExecution,
    UUID jobId,
    String filename
  ) {
    String errorMessage = getDetailedErrorMessage(jobExecution);
    List<Throwable> exceptions = jobExecution.getAllFailureExceptions();

    long duration = (
        jobExecution.getEndTime() != null && jobExecution.getStartTime() != null
      )
      ? ChronoUnit.MILLIS.between(
        jobExecution.getStartTime(),
        jobExecution.getEndTime()
      )
      : 0;

    if (!exceptions.isEmpty()) {
      Throwable rootCause = exceptions.getFirst();
      log.error(
        "Job {} FALHOU para arquivo {} - Status: {} - Duração: {}ms - Erro: {}",
        jobId,
        filename,
        jobExecution.getStatus(),
        duration,
        errorMessage,
        rootCause
      );

      logStepFailures(jobExecution.getStepExecutions());
    } else {
      log.error(
        "Job {} FALHOU para arquivo {} - Status: {} - Erro: {}",
        jobId,
        filename,
        jobExecution.getStatus(),
        errorMessage
      );
    }

    updateJobStatus(jobExecution, ImportJobStatus.FAILED, errorMessage);
  }

  private void handleJobSuccess(
    JobExecution jobExecution,
    UUID jobId,
    String filename
  ) {
    long duration = (
        jobExecution.getEndTime() != null && jobExecution.getStartTime() != null
      )
      ? ChronoUnit.MILLIS.between(
        jobExecution.getStartTime(),
        jobExecution.getEndTime()
      )
      : 0;

    long totalRead = 0;
    long totalWritten = 0;
    long totalSkipped = 0;

    for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
      totalRead += stepExecution.getReadCount();
      totalWritten += stepExecution.getWriteCount();
      totalSkipped += stepExecution.getSkipCount();
    }

    log.info(
      "Job {} CONCLUÍDO com sucesso para arquivo {} - Duração: {}ms - Lidos: {} - Escritos: {} - Erros: {}",
      jobId,
      filename,
      duration,
      totalRead,
      totalWritten,
      totalSkipped
    );

    updateJobStatus(jobExecution, ImportJobStatus.COMPLETED, null);
  }

  private void logStepFailures(Iterable<StepExecution> stepExecutions) {
    for (StepExecution stepExecution : stepExecutions) {
      if (!stepExecution.getFailureExceptions().isEmpty()) {
        log.error(
          "Step '{}' falhou - Lidos: {} - Escritos: {} - Ignorados: {} - Commits: {}",
          stepExecution.getStepName(),
          stepExecution.getReadCount(),
          stepExecution.getWriteCount(),
          stepExecution.getSkipCount(),
          stepExecution.getCommitCount()
        );
      }
    }
  }

  String getDetailedErrorMessage(JobExecution jobExecution) {
    List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
    if (exceptions.isEmpty()) {
      return "Erro desconhecido durante o processamento";
    }

    StringBuilder sb = new StringBuilder();

    Throwable rootCause = exceptions.getFirst();
    sb
      .append(rootCause.getClass().getSimpleName())
      .append(": ")
      .append(rootCause.getMessage());

    for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
      if (!stepExecution.getFailureExceptions().isEmpty()) {
        sb.append(" (Step: ").append(stepExecution.getStepName()).append(")");
        break;
      }
    }

    return sb.toString();
  }

  void updateJobStatus(
    JobExecution jobExecution,
    ImportJobStatus status,
    String errorMessage
  ) {
    String jobIdStr = jobExecution
      .getJobParameters()
      .getString(ID_PARAMETER_NAME);
    if (jobIdStr != null) {
      UUID jobId = UUID.fromString(jobIdStr);
      importService.updateJobStatus(jobId, status, errorMessage);
    }
  }
}
