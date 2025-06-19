/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.service;

import com.kleberrhuan.houer.school.domain.model.DataType;
import com.kleberrhuan.houer.school.domain.model.MetricDictionary;
import com.kleberrhuan.houer.school.domain.repository.MetricDictionaryRepository;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricCatalogService {

  private final MetricDictionaryRepository metricDictionaryRepository;

  @PostConstruct
  public void initializeCache() {
    Set<String> metrics = getAllValidMetricCodes();
    log.info(
      "Cache de métricas inicializado com {} métricas válidas",
      metrics.size()
    );
  }

  public boolean isValidMetric(String metricCode) {
    return getAllValidMetricCodes().contains(metricCode);
  }

  @Cacheable(value = "metricCatalog", key = "#metricCode")
  public DataType getDataType(String metricCode) {
    log.debug("Buscando tipo de dado para métrica: {}", metricCode);

    return metricDictionaryRepository
      .findById(metricCode)
      .map(MetricDictionary::getDataType)
      .orElseThrow(() ->
        new IllegalArgumentException("Métrica não encontrada: " + metricCode)
      );
  }

  @Cacheable(value = "metricCatalog", unless = "#result.isEmpty()")
  public Set<String> getAllValidMetricCodes() {
    log.debug("Carregando métricas válidas do banco de dados");
    Set<String> metrics = metricDictionaryRepository.findAllMetricCodes();
    log.info("Carregadas {} métricas válidas do banco", metrics.size());
    return metrics;
  }
}
