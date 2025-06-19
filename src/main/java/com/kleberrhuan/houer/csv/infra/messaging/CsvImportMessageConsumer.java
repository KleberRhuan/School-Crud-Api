/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.messaging;

import static com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Jobs.*;

import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.infra.exception.RabbitMqException;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportQueueMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

/** Consumer para processar mensagens de importação CSV do RabbitMQ. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CsvImportMessageConsumer {

  private final JobLauncher jobLauncher;
  private final Job schoolImportJob;
  private final CsvImportService csvImportService;

  @RabbitListener(queues = CsvImportConstants.Queues.CSV_IMPORT_QUEUE)
  public void handleImportMessage(CsvImportQueueMessage message) {
    MDC.setContextMap(Map.of("jobId", message.jobId().toString()));
    log.info("Mensagem recebida para importação CSV");

    try {
      JobParameters params = buildJobParameters(message);
      jobLauncher.run(schoolImportJob, params);
      log.info("Job de importação disparado com sucesso");
    } catch (
      JobExecutionAlreadyRunningException
      | JobRestartException
      | JobInstanceAlreadyCompleteException
      | JobParametersInvalidException e
    ) {
      handleFailure(message, "Job inválido ou já em execução", e);
    } catch (Exception e) {
      handleFailure(message, "Falha inesperada ao iniciar job", e);
    } finally {
      MDC.clear();
    }
  }

  /* ---------- helpers ---------- */

  private JobParameters buildJobParameters(CsvImportQueueMessage m) {
    return new JobParametersBuilder()
      .addString(ID_PARAMETER_NAME, m.jobId().toString())
      .addString(FILENAME_PARAMETER_NAME, m.filename())
      .addString(FILE_URI_PARAMETER_NAME, m.fileUri().toString())
      .addString(DESCRIPTION_PARAMETER_NAME, m.description())
      .addLong(USER_ID_PARAMETER_NAME, m.userId())
      .addLong(TIMESTAMP_PARAMETER_NAME, System.currentTimeMillis())
      .toJobParameters();
  }

  private void handleFailure(
    CsvImportQueueMessage msg,
    String reason,
    Exception root
  ) {
    log.warn(
      "Falha ao iniciar job {}: {} - {}",
      msg.jobId(),
      reason,
      root.getMessage()
    );

    try {
      csvImportService.updateJobStatus(
        msg.jobId(),
        ImportJobStatus.FAILED,
        reason + ": " + root.getMessage()
      );
    } catch (Exception updateErr) {
      log.error(
        "Não foi possível atualizar status do job {}: {}",
        msg.jobId(),
        updateErr.getMessage()
      );
    }

    throw new RabbitMqException(
      String.format(
        "Erro ao processar job %s: %s",
        msg.jobId(),
        root.getMessage()
      )
    );
  }
}
