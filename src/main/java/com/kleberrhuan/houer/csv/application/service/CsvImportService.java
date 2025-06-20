/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.service;

import com.kleberrhuan.houer.common.application.factory.PageableFactory;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.csv.application.port.CsvValidator;
import com.kleberrhuan.houer.csv.application.port.NotificationService;
import com.kleberrhuan.houer.csv.application.port.StorageService;
import com.kleberrhuan.houer.csv.domain.exception.ActiveImportJobException;
import com.kleberrhuan.houer.csv.domain.factory.CsvImportNotificationFactory;
import com.kleberrhuan.houer.csv.domain.model.*;
import com.kleberrhuan.houer.csv.domain.repository.CsvImportJobRepository;
import com.kleberrhuan.houer.csv.infra.exception.CsvProcessingException;
import com.kleberrhuan.houer.csv.infra.messaging.CsvImportMessagePublisher;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportQueueMessage;
import jakarta.validation.constraints.NotNull;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

  private final CsvImportJobRepository jobRepository;
  private final CsvValidator<CsvSchoolRecord> csvValidator;
  private final CsvImportMessagePublisher messagePublisher;
  private final PageableFactory pageableFactory;
  private final NotificationService notificationService;
  private final StorageService storageService;

  @Transactional
  public UUID startImport(@NonNull MultipartFile file, @NotNull Long userId) {
    validateNoActiveJob(userId);
    validateFile(file);

    try (InputStream in = file.getInputStream()) {
      List<CsvSchoolRecord> records = parseCsvToList(
        in,
        file.getOriginalFilename()
      );

      String description = file.getOriginalFilename();
      CsvImportJob job = createAndSaveJob(file, description, records);

      URI fileUri = storageService.store(file, job.getId());

      publishQueueMessage(job, fileUri, description, userId);
      notifyEnqueued(job);

      log.info(
        "Job {} enfileirado ({} registros) – arquivo {} armazenado em {}",
        job.getId(),
        records.size(),
        file.getOriginalFilename(),
        fileUri
      );

      return job.getId();
    } catch (Exception e) {
      throw new CsvProcessingException(
        "Erro ao iniciar importação: " + e.getMessage(),
        e
      );
    }
  }

  @Transactional
  public void updateJobStatus(
    @NonNull UUID jobId,
    @NonNull ImportJobStatus status,
    String errorMessage
  ) {
    CsvImportJob job = findJobById(jobId);
    updateTimestamps(job, status);
    if (errorMessage != null) {
      job.setErrorMessage(errorMessage);
    }
    jobRepository.save(job);

    CsvImportNotification notification =
      switch (status) {
        case RUNNING -> CsvImportNotificationFactory.started(job);
        case COMPLETED -> CsvImportNotificationFactory.completed(job);
        case FAILED -> CsvImportNotificationFactory.failed(job, errorMessage);
        default -> CsvImportNotificationFactory.of(
          job,
          status,
          statusMessage(status, errorMessage)
        );
      };

    notificationService.send(notification);
  }

  private void validateNoActiveJob(Long userId) {
    boolean exists = jobRepository.existsByCreatedByAndStatusIn(
      userId,
      List.of(ImportJobStatus.PENDING, ImportJobStatus.RUNNING)
    );
    if (exists) {
      throw new ActiveImportJobException(
        "Usuário %d já possui importação ativa".formatted(userId)
      );
    }
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new CsvProcessingException("Arquivo vazio");
    }
  }

  private List<CsvSchoolRecord> parseCsvToList(
    InputStream in,
    String filename
  ) {
    return csvValidator.validateToList(in, filename);
  }

  private CsvImportJob createAndSaveJob(
    MultipartFile file,
    String description,
    List<CsvSchoolRecord> records
  ) {
    CsvImportJob job = new CsvImportJob();
    job.setFilename(file.getOriginalFilename());
    job.setDescription(description);
    job.setStatus(ImportJobStatus.PENDING);
    job.setTotalRecords(records.size());
    job.setProcessedRecords(0);
    job.setErrorRecords(0);
    return jobRepository.save(job);
  }

  private void publishQueueMessage(
    CsvImportJob job,
    URI fileUri,
    String description,
    Long userId
  ) {
    CsvImportQueueMessage msg = new CsvImportQueueMessage(
      job.getId(),
      job.getFilename(),
      fileUri,
      description,
      userId,
      Instant.now()
    );

    messagePublisher.publishImportMessage(msg);
  }

  private void notifyEnqueued(CsvImportJob job) {
    CsvImportNotification notification = CsvImportNotificationFactory.of(
      job,
      ImportJobStatus.PENDING,
      "Importação enfileirada para processamento"
    );
    notificationService.send(notification);
  }

  private void updateTimestamps(CsvImportJob job, ImportJobStatus status) {
    if (status == ImportJobStatus.RUNNING && job.getStartedAt() == null) {
      job.setStartedAt(Instant.now());
    }
    if (
      status == ImportJobStatus.COMPLETED || status == ImportJobStatus.FAILED
    ) {
      job.setFinishedAt(Instant.now());
    }
    job.setStatus(status);
  }

  private String statusMessage(ImportJobStatus status, String errorMessage) {
    Map<ImportJobStatus, String> map = new EnumMap<>(ImportJobStatus.class);
    map.put(ImportJobStatus.RUNNING, "Processamento iniciado");
    map.put(ImportJobStatus.COMPLETED, "Importação concluída com sucesso");
    map.put(
      ImportJobStatus.FAILED,
      errorMessage != null ? errorMessage : "Erro desconhecido"
    );
    return map.getOrDefault(status, "Status atualizado");
  }

  public CsvImportJob findJobById(@NonNull UUID jobId) {
    return jobRepository
      .findById(jobId)
      .orElseThrow(() ->
        new com.kleberrhuan.houer.csv.domain.exception.ImportJobNotFoundException(
          jobId
        )
      );
  }

  public PaginatedResponse<List<CsvImportJob>> findJobsByUser(
    Long userId,
    PageableRequest pageableRequest
  ) {
    var pageable = pageableFactory.create(pageableRequest);
    var page = jobRepository.findByCreatedBy(userId, pageable);
    return PaginatedResponse.of(page);
  }

  public PaginatedResponse<List<CsvImportJob>> findJobsByStatus(
    ImportJobStatus status,
    PageableRequest pageableRequest
  ) {
    var pageable = pageableFactory.create(pageableRequest);
    var page = jobRepository.findByStatus(status, pageable);
    return PaginatedResponse.of(page);
  }

  public PaginatedResponse<List<CsvImportJob>> findAllJobs(
    PageableRequest pageableRequest
  ) {
    var pageable = pageableFactory.create(pageableRequest);
    var page = jobRepository.findAll(pageable);
    return PaginatedResponse.of(page);
  }

  @Transactional
  public void cancelJob(UUID jobId, Long userId) {
    CsvImportJob job = findJobById(jobId);

    if (userId != null && !userId.equals(job.getCreatedBy())) {
      throw new com.kleberrhuan.houer.common.domain.exception.BusinessException(
        org.springframework.http.HttpStatus.FORBIDDEN,
        com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType.FORBIDDEN,
        com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey.of(
          "error.csv.import.job.unauthorized"
        )
      );
    }

    if (
      job.getStatus() == ImportJobStatus.COMPLETED ||
      job.getStatus() == ImportJobStatus.FAILED
    ) {
      throw new com.kleberrhuan.houer.common.domain.exception.BusinessException(
        org.springframework.http.HttpStatus.CONFLICT,
        com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType.BUSINESS_ERROR,
        com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey.of(
          "error.csv.import.job.already.finished"
        ),
        "Job já finalizado"
      );
    }

    updateJobStatus(
      jobId,
      ImportJobStatus.FAILED,
      "Importação cancelada pelo usuário"
    );
  }

  @Transactional
  public void updateJobProgress(
    UUID jobId,
    int processedRecords,
    int errorRecords
  ) {
    CsvImportJob job = findJobById(jobId);

    job.setProcessedRecords(job.getProcessedRecords() + processedRecords);
    job.setErrorRecords(job.getErrorRecords() + errorRecords);

    jobRepository.save(job);
    notificationService.send(CsvImportNotificationFactory.progress(job));
  }
}
