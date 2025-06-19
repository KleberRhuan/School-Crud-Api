/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.repository;

import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositório para jobs de importação CSV. */
@Repository
public interface CsvImportJobRepository
  extends JpaRepository<CsvImportJob, UUID> {
  /** Busca jobs por status com paginação. */
  Page<CsvImportJob> findByStatus(ImportJobStatus status, Pageable pageable);

  /** Busca jobs de um usuário específico. */
  Page<CsvImportJob> findByCreatedBy(Long createdBy, Pageable pageable);

  /** Verifica se usuário tem jobs ativos (PENDING ou RUNNING). */
  boolean existsByCreatedByAndStatusIn(
    Long userId,
    List<ImportJobStatus> statuses
  );
}
