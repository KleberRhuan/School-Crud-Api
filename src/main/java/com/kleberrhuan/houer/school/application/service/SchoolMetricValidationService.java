/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.service;

import com.kleberrhuan.houer.csv.application.port.MetricValidationService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolMetricValidationService implements MetricValidationService {

  private final MetricCatalogService metricCatalogService;

  @Override
  public Set<String> getAllValidMetricCodes() {
    return metricCatalogService.getAllValidMetricCodes();
  }

  @Override
  public boolean isValidMetric(String metricCode) {
    return metricCatalogService.isValidMetric(metricCode);
  }
}
