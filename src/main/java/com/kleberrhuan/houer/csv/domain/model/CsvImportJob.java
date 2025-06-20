/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.model;

import com.kleberrhuan.houer.common.infra.persistence.Auditable;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Entidade para controle de jobs de importação CSV. */
@Entity
@Table(name = "csv_import_job", schema = "csv")
@Getter
@Setter
public class CsvImportJob extends Auditable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private String filename;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ImportJobStatus status;

  @Column(name = "total_records")
  private Integer totalRecords = 0;

  @Column(name = "processed_records")
  private Integer processedRecords = 0;

  @Column(name = "error_records")
  private Integer errorRecords = 0;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Version
  private Long version;
}
