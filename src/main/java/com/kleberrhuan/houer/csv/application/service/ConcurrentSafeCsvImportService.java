/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.service;

import com.kleberrhuan.houer.common.infra.exception.InfrastructureException;
import com.kleberrhuan.houer.csv.domain.exception.ActiveImportJobException;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.domain.repository.CsvImportJobRepository;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcurrentSafeCsvImportService {

  private static final Duration USER_LOCK_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration JOB_LOCK_TIMEOUT = Duration.ofSeconds(5);

  private final CsvImportService csvImportService;
  private final CsvImportJobRepository jobRepository;
  private final LockManager lockManager = new LockManager();

  @Transactional
  public UUID startImport(MultipartFile file, Long userId) {
    ReentrantLock userLock = lockManager.acquireUserLock(userId);
    try {
      validateNoActiveJob(userId);
      UUID jobId = csvImportService.startImport(file, userId);
      log.info("Job {} iniciado para usuário {}", jobId, userId);
      return jobId;
    } finally {
      lockManager.releaseUserLock(userId, userLock);
    }
  }

  public void cancelJob(UUID jobId, Long userId) {
    ReentrantLock jobLock = lockManager.acquireJobLock(jobId);
    try {
      csvImportService.cancelJob(jobId, userId);
      log.info("Job {} cancelado pelo usuário {}", jobId, userId);
    } finally {
      lockManager.releaseJobLock(jobId, jobLock);
    }
  }

  private void validateNoActiveJob(Long userId) {
    Set<ImportJobStatus> ativos = EnumSet.of(
      ImportJobStatus.PENDING,
      ImportJobStatus.RUNNING
    );
    if (
      jobRepository.existsByCreatedByAndStatusIn(
        userId,
        ativos.stream().toList()
      )
    ) {
      log.warn("Usuário {} já possui importação ativa", userId);
      throw new ActiveImportJobException(
        "Você já possui uma importação em andamento. Aguarde a conclusão."
      );
    }
  }

  static class LockManager {

    private final ConcurrentHashMap<Long, ReentrantLock> userLocks =
      new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> jobLocks =
      new ConcurrentHashMap<>();

    ReentrantLock acquireUserLock(Long userId) {
      return acquireLock(
        userLocks,
        userId,
        ConcurrentSafeCsvImportService.USER_LOCK_TIMEOUT,
        id ->
          new ActiveImportJobException("Falha ao obter lock para usuário " + id)
      );
    }

    ReentrantLock acquireJobLock(UUID jobId) {
      return acquireLock(
        jobLocks,
        jobId,
        ConcurrentSafeCsvImportService.JOB_LOCK_TIMEOUT,
        id -> new RuntimeException("Não foi possível obter lock para job " + id)
      );
    }

    private <T> ReentrantLock acquireLock(
      ConcurrentHashMap<T, ReentrantLock> map,
      T key,
      Duration timeout,
      java.util.function.Function<T, RuntimeException> onFail
    ) {
      ReentrantLock lock = map.computeIfAbsent(key, k -> new ReentrantLock());
      try {
        if (!lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
          throw onFail.apply(key);
        }
        log.debug("Lock adquirido para {}", key);
        return lock;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw InfrastructureException.defaultException(
          "Interrompido ao aguardar lock"
        );
      }
    }

    void releaseUserLock(Long userId, ReentrantLock lock) {
      releaseLock(userLocks, userId, lock);
    }

    void releaseJobLock(UUID jobId, ReentrantLock lock) {
      releaseLock(jobLocks, jobId, lock);
    }

    private <T> void releaseLock(
      ConcurrentHashMap<T, ReentrantLock> map,
      T key,
      ReentrantLock lock
    ) {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.debug("Lock liberado para {}", key);
        if (!lock.hasQueuedThreads()) {
          map.remove(key, lock);
          log.debug("Lock removido do pool para {}", key);
        }
      }
    }
  }
}
