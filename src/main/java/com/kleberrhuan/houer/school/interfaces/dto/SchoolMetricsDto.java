/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.dto;

import java.time.Instant;
import java.util.Map;

public record SchoolMetricsDto(
  Long schoolCode,
  Map<String, Long> metrics,
  Instant createdAt,
  Instant updatedAt
) {}
