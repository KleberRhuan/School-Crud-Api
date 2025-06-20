/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.domain.exception.HeaderValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HeaderValidator {

  private final CsvColumnMetadataService metadataService;

  private static final Set<String> ALL_VALID_COLUMNS = Arrays
    .stream(CsvSchoolColumn.values())
    .map(Enum::name)
    .collect(Collectors.toUnmodifiableSet());

  private static final char BOM = '\uFEFF';

  public String[] validate(String[] header, String filename) {
    if (header.length == 0) {
      throw new HeaderValidationException(filename, 0);
    }

    Set<String> headerSet = Arrays
      .stream(header)
      .map(this::cleanColumnName)
      .collect(Collectors.toCollection(LinkedHashSet::new));

    // Verificar se há colunas inválidas (não reconhecidas)
    Set<String> invalidColumns = headerSet
      .stream()
      .filter(column -> !ALL_VALID_COLUMNS.contains(column))
      .collect(Collectors.toSet());

    if (!invalidColumns.isEmpty()) {
      throw new HeaderValidationException(filename, invalidColumns, true);
    }

    // Verificar se as colunas obrigatórias básicas estão presentes
    Set<String> requiredBasicColumns = metadataService
      .getRequiredHeaders()
      .stream()
      .map(Enum::name)
      .collect(Collectors.toSet());

    Set<String> missingBasicColumns = requiredBasicColumns
      .stream()
      .filter(column -> !headerSet.contains(column))
      .collect(Collectors.toSet());

    if (!missingBasicColumns.isEmpty()) {
      throw new HeaderValidationException(filename, missingBasicColumns);
    }

    log.debug(
      "Header de {} validado com sucesso: {} colunas presentes (de {} possíveis)",
      filename,
      headerSet.size(),
      ALL_VALID_COLUMNS.size()
    );

    return headerSet.toArray(String[]::new);
  }

  /** Remove BOM UTF-8 e espaços em branco do nome da coluna */
  private String cleanColumnName(String columnName) {
    if (columnName == null) {
      return null;
    }

    String cleaned = columnName.trim();

    if (!cleaned.isEmpty() && cleaned.charAt(0) == BOM) {
      cleaned = cleaned.substring(1).trim();
    }

    return cleaned;
  }
}
