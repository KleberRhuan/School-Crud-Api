/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.service;

import com.kleberrhuan.houer.common.infra.aspect.NoTracing;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@NoTracing
public class CsvColumnMetadataService {

  private static final Map<String, CsvSchoolColumn> COLUMN_LOOKUP =
    CsvSchoolColumn
      .getAllColumns()
      .stream()
      .collect(
        Collectors.toUnmodifiableMap(CsvSchoolColumn::name, Function.identity())
      );

  private static final Set<CsvSchoolColumn> METRIC_COLUMNS =
    CsvSchoolColumn.getMetricColumns();

  private static final Set<CsvSchoolColumn> HEADER_COLUMNS =
    CsvSchoolColumn.getRequiredHeaders();

  public Set<CsvSchoolColumn> getRequiredHeaders() {
    return HEADER_COLUMNS;
  }

  public Set<CsvSchoolColumn> getMetricColumns() {
    return METRIC_COLUMNS;
  }

  public Optional<CsvSchoolColumn> getColumnInfo(String columnName) {
    if (columnName == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(COLUMN_LOOKUP.get(columnName));
  }

  public int getTotalRequiredColumnsCount() {
    return HEADER_COLUMNS.size();
  }
}
