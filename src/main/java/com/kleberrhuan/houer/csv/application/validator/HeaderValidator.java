/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import com.kleberrhuan.houer.csv.domain.exception.HeaderValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeaderValidator {

  private static final Set<String> EXPECTED_COLUMNS = Arrays
    .stream(CsvSchoolColumn.values())
    .map(Enum::name)
    .collect(Collectors.toUnmodifiableSet());

  private static final int EXPECTED_SIZE = EXPECTED_COLUMNS.size();
  private static final char BOM = '\uFEFF';

  public String[] validate(String[] header, String filename) {
    if (header.length == 0) {
      throw new HeaderValidationException(filename, 0);
    }

    Set<String> headerSet = Arrays
      .stream(header)
      .map(this::cleanColumnName)
      .collect(Collectors.toCollection(LinkedHashSet::new));

    Set<String> extraColumns = headerSet
      .stream()
      .filter(column -> !EXPECTED_COLUMNS.contains(column))
      .collect(Collectors.toSet());

    if (!extraColumns.isEmpty()) {
      throw new HeaderValidationException(filename, extraColumns, true);
    }

    Set<String> missingColumns = EXPECTED_COLUMNS
      .stream()
      .filter(column -> !headerSet.contains(column))
      .collect(Collectors.toSet());

    if (!missingColumns.isEmpty()) {
      throw new HeaderValidationException(filename, missingColumns);
    }

    if (headerSet.size() != EXPECTED_SIZE) {
      throw new HeaderValidationException(filename, headerSet.size());
    }

    log.debug(
      "Header de {} validado com sucesso: {} colunas presentes",
      filename,
      headerSet.size()
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
