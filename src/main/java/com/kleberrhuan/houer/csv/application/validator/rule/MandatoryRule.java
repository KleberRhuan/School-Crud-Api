/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator.rule;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.domain.exception.ValidationErrorAggregator;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public final class MandatoryRule implements RowRule {

  private final CsvColumnMetadataService metadataService;

  @Override
  public void validate(RowContext context) {
    String[] headers = context.headers();
    String[] values = context.values();
    ValidationErrorAggregator errorAggregator = new ValidationErrorAggregator();

    for (int i = 0; i < headers.length && i < values.length; i++) {
      String columnName = headers[i].trim();
      String value = values[i];

      Optional<CsvSchoolColumn> columnInfo = metadataService.getColumnInfo(
        columnName
      );
      if (columnInfo.isPresent() && columnInfo.get().isRequired()) {
        if (value == null || value.trim().isEmpty()) {
          errorAggregator.addError(
            context.lineNumber(),
            columnName,
            "Campo obrigatório não pode estar vazio"
          );
        }
      }
    }

    errorAggregator.throwIfErrors();
  }
}
