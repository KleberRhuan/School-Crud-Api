/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.model;

import jakarta.persistence.*;
import lombok.*;

/** Entidade que representa o dicionário de métricas válidas para escolas. */
@Entity
@Table(name = "metric_dictionary", schema = "school")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MetricDictionary {

  @Id
  @Column(name = "metric_code")
  private String metricCode;

  @Column(name = "metric_name", nullable = false)
  private String metricName;

  @Enumerated(EnumType.STRING)
  @Column(name = "data_type", nullable = false)
  private DataType dataType;
}
