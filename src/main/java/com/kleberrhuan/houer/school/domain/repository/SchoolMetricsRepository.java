/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.repository;

import com.kleberrhuan.houer.school.domain.model.SchoolMetrics;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositório para operações relacionadas às métricas das escolas. */
@Repository
public interface SchoolMetricsRepository
  extends JpaRepository<SchoolMetrics, Long> {
  @Query(
    value = "SELECT sm.* FROM school.school_metrics_jsonb sm WHERE " +
    "sm.metrics->>:metricName IS NOT NULL",
    nativeQuery = true
  )
  List<SchoolMetrics> findByMetricExists(
    @Param("metricName") String metricName
  );

  @Query(
    value = "SELECT sm.* FROM school.school_metrics_jsonb sm WHERE " +
    "CAST(sm.metrics->>:metricName AS BIGINT) = :value",
    nativeQuery = true
  )
  List<SchoolMetrics> findByMetricValue(
    @Param("metricName") String metricName,
    @Param("value") Long value
  );
}
