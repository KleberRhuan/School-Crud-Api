/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Map;

public record CsvSchoolRecord(
  @NotBlank String nomeDep,
  @NotBlank String de,
  @NotBlank String mun,
  @NotBlank String distr,
  @NotBlank String codesc,
  @NotBlank String nomesc,
  @Positive Long tipoesc,
  @NotBlank String tipoescDesc,
  @PositiveOrZero Short codSit,
  @NotBlank Map<CsvSchoolColumn, Long> metrics
) {}
