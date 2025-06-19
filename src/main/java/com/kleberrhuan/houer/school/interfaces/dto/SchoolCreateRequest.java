/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** DTO para criação de escola. */
public record SchoolCreateRequest(
  @NotNull @Positive Long code,
  @NotNull @Size(max = 200) String schoolName,
  @Size(max = 100) String administrativeDependency,
  @Size(max = 10) String stateCode,
  @Size(max = 100) String municipality,
  @Size(max = 100) String district,
  Short schoolType,
  @Size(max = 100) String schoolTypeDescription,
  Short situationCode,
  Long schoolCode,
  Map<String, Long> metrics
) {}
