/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch.listener;

import static com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Jobs.ID_PARAMETER_NAME;

import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvImportStepListener implements StepExecutionListener {

  private final CsvImportService importService;
  private final ConcurrentHashMap<UUID, ReentrantLock> progressLocks =
    new ConcurrentHashMap<>();

  @Override
  public void beforeStep(StepExecution stepExecution) {
    String jobIdStr = stepExecution
      .getJobParameters()
      .getString(ID_PARAMETER_NAME);
    if (jobIdStr != null) {
      UUID jobId = UUID.fromString(jobIdStr);
      log.info("Iniciando step de importação para job {}", jobId);
    }
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    String jobIdStr = stepExecution
      .getJobParameters()
      .getString(ID_PARAMETER_NAME);

    if (jobIdStr != null) {
      UUID jobId = UUID.fromString(jobIdStr);

      long readCount = stepExecution.getReadCount();
      long writeCount = stepExecution.getWriteCount();
      long skipCount = stepExecution.getSkipCount();

      ReentrantLock lock = progressLocks.computeIfAbsent(
        jobId,
        k -> new ReentrantLock()
      );
      lock.lock();
      try {
        importService.updateJobProgress(
          jobId,
          (int) writeCount,
          (int) skipCount
        );

        log.info(
          "Step concluído para job {}: lidos={}, escritos={}, errors={}",
          jobId,
          readCount,
          writeCount,
          skipCount
        );
      } finally {
        lock.unlock();

        if (
          ExitStatus.COMPLETED.equals(stepExecution.getExitStatus()) ||
          ExitStatus.FAILED.equals(stepExecution.getExitStatus())
        ) {
          progressLocks.remove(jobId);
        }
      }
    }

    return stepExecution.getExitStatus();
  }
}
