/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** DTO para atualização de escola. */
public record SchoolUpdateRequest(
  @Nullable @Size(max = 200) String schoolName,
  @Nullable @Size(max = 100) String administrativeDependency,
  @Nullable @Size(max = 10) String stateCode,
  @Nullable @Size(max = 100) String municipality,
  @Nullable @Size(max = 100) String district,
  @Nullable Short schoolType,
  @Nullable @Size(max = 100) String schoolTypeDescription,
  @Nullable Short situationCode,
  @Nullable Long schoolCode,
  @Nullable Map<String, Long> metrics
) {}
