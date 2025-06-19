/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.port;

import java.util.Set;

public interface MetricValidationService {
  Set<String> getAllValidMetricCodes();

  boolean isValidMetric(String metricCode);
}
