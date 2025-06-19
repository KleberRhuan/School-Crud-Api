/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.repository;

import com.kleberrhuan.houer.school.domain.model.MetricDictionary;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repositório para operações relacionadas ao dicionário de métricas. */
@Repository
public interface MetricDictionaryRepository
  extends JpaRepository<MetricDictionary, String> {
  @Query("SELECT m.metricCode FROM MetricDictionary m")
  Set<String> findAllMetricCodes();

  boolean existsByMetricCode(String metricCode);
}
