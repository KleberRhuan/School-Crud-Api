/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.model;

import com.kleberrhuan.houer.common.infra.persistence.Auditable;
import jakarta.persistence.*;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Entidade que representa as métricas de uma escola em formato JSONB. */
@Entity
@Table(name = "school_metrics_jsonb", schema = "school")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SchoolMetrics extends Auditable<Long> {

  @Id
  @Column(name = "school_code")
  private Long schoolCode;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "school_code")
  private School school;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metrics", columnDefinition = "jsonb")
  private Map<String, Long> metrics;

  public SchoolMetrics(Map<String, Long> metrics) {
    this.metrics = metrics;
  }

  public void updateMetrics(Map<String, Long> metrics) {
    this.metrics = metrics;
  }
}
