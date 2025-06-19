/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvImportJobListener Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvImportJobListenerTest {

  @Mock
  private CsvImportService csvImportService;

  private CsvImportJobListener listener;
  private UUID jobId;
  private String filename;

  @BeforeEach
  void setUp() {
    listener = new CsvImportJobListener(csvImportService);
    jobId = UUID.randomUUID();
    filename = "test.csv";
  }

  @Test
  @DisplayName("Deve logar início do job e atualizar status para RUNNING")
  void shouldLogJobStartAndUpdateStatusToRunning() {
    // Given
    JobExecution jobExecution = createJobExecution(BatchStatus.STARTING);

    // When
    listener.beforeJob(jobExecution);

    // Then
    verify(csvImportService)
      .updateJobStatus(jobId, ImportJobStatus.RUNNING, null);
  }

  @Test
  @DisplayName("Deve logar sucesso do job com estatísticas agregadas")
  void shouldLogJobSuccessWithAggregatedStats() {
    // Given
    StepExecution step1 = addStepExecution(null, "step1", 100, 95, 5);
    StepExecution step2 = addStepExecution(null, "step2", 50, 45, 5);
    JobExecution jobExecution = createJobExecutionWithSteps(
      BatchStatus.COMPLETED,
      step1,
      step2
    );

    // When
    listener.afterJob(jobExecution);

    // Then
    verify(csvImportService)
      .updateJobStatus(jobId, ImportJobStatus.COMPLETED, null);
  }

  @Test
  @DisplayName("Deve logar falha do job com contexto detalhado de erro")
  void shouldLogJobFailureWithDetailedErrorContext() {
    // Given
    RuntimeException exception = new RuntimeException(
      "Database connection failed"
    );

    StepExecution stepExecution = addStepExecution(
      null,
      "schoolImportStep",
      50,
      25,
      25
    );
    when(stepExecution.getFailureExceptions())
      .thenReturn(java.util.List.of(exception));

    JobExecution jobExecution = createJobExecutionWithSteps(
      BatchStatus.FAILED,
      stepExecution
    );
    when(jobExecution.getAllFailureExceptions())
      .thenReturn(java.util.List.of(exception));

    // When
    listener.afterJob(jobExecution);

    // Then
    verify(csvImportService)
      .updateJobStatus(
        eq(jobId),
        eq(ImportJobStatus.FAILED),
        contains("RuntimeException: Database connection failed")
      );
  }

  @Test
  @DisplayName("Deve construir mensagem de erro detalhada com contexto do step")
  void shouldBuildDetailedErrorMessageWithStepContext() {
    // Given
    RuntimeException exception = new RuntimeException("Validation failed");

    StepExecution stepExecution = addStepExecution(
      null,
      "csvProcessingStep",
      100,
      50,
      50
    );
    when(stepExecution.getFailureExceptions())
      .thenReturn(java.util.List.of(exception));

    JobExecution jobExecution = createJobExecutionWithSteps(
      BatchStatus.FAILED,
      stepExecution
    );
    when(jobExecution.getAllFailureExceptions())
      .thenReturn(java.util.List.of(exception));

    // When
    String errorMessage = listener.getDetailedErrorMessage(jobExecution);

    // Then
    assertThat(errorMessage)
      .contains("RuntimeException: Validation failed")
      .contains("(Step: csvProcessingStep)");
  }

  @Test
  @DisplayName("Deve retornar mensagem padrão quando não há exceções")
  void shouldReturnDefaultMessageWhenNoExceptions() {
    // Given
    JobExecution jobExecution = createJobExecution(BatchStatus.FAILED);

    // When
    String errorMessage = listener.getDetailedErrorMessage(jobExecution);

    // Then
    assertThat(errorMessage)
      .isEqualTo("Erro desconhecido durante o processamento");
  }

  private JobExecution createJobExecution(BatchStatus status) {
    JobParameters params = new JobParametersBuilder()
      .addString("jobId", jobId.toString())
      .addString("filename", filename)
      .toJobParameters();

    JobExecution jobExecution = new JobExecution(1L, params);
    jobExecution.setStatus(status);
    jobExecution.setStartTime(LocalDateTime.now().minusMinutes(5));
    jobExecution.setEndTime(LocalDateTime.now());

    return jobExecution;
  }

  private StepExecution addStepExecution(
    JobExecution jobExecution,
    String stepName,
    int readCount,
    int writeCount,
    int skipCount
  ) {
    StepExecution stepExecution = mock(StepExecution.class);

    when(stepExecution.getStepName()).thenReturn(stepName);
    when(stepExecution.getReadCount()).thenReturn((long) readCount);
    when(stepExecution.getWriteCount()).thenReturn((long) writeCount);
    when(stepExecution.getSkipCount()).thenReturn((long) skipCount);
    when(stepExecution.getCommitCount()).thenReturn(1L);
    when(stepExecution.getFailureExceptions()).thenReturn(java.util.List.of());

    return stepExecution;
  }

  private JobExecution createJobExecutionWithSteps(
    BatchStatus status,
    StepExecution... steps
  ) {
    JobExecution jobExecution = mock(JobExecution.class);
    JobParameters params = new JobParametersBuilder()
      .addString("jobId", jobId.toString())
      .addString("filename", filename)
      .toJobParameters();

    when(jobExecution.getJobParameters()).thenReturn(params);
    when(jobExecution.getStatus()).thenReturn(status);
    when(jobExecution.getStartTime())
      .thenReturn(LocalDateTime.now().minusMinutes(5));
    when(jobExecution.getEndTime()).thenReturn(LocalDateTime.now());
    when(jobExecution.getStepExecutions()).thenReturn(java.util.List.of(steps));
    when(jobExecution.getAllFailureExceptions())
      .thenReturn(java.util.List.of());

    return jobExecution;
  }
}
