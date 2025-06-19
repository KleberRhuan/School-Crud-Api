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

/**
 * Regra que valida campos numéricos. Campos marcados como numéricos devem conter apenas números inteiros não negativos
 * quando não vazios.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public final class NumericRule implements RowRule {

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
      if (
        columnInfo.isPresent() &&
        columnInfo.get().isNumeric() &&
        value != null &&
        !value.trim().isEmpty()
      ) {
        validateNumericValue(
          value.trim(),
          columnName,
          context.lineNumber(),
          errorAggregator
        );
      }
    }

    errorAggregator.throwIfErrors();
  }

  private void validateNumericValue(
    String value,
    String columnName,
    int lineNumber,
    ValidationErrorAggregator errorAggregator
  ) {
    try {
      long numericValue = Long.parseLong(value);

      if (numericValue < 0) {
        errorAggregator.addError(
          lineNumber,
          columnName,
          String.format(
            "Valor '%s' deve ser um número inteiro não negativo",
            value
          )
        );
      }
    } catch (NumberFormatException e) {
      errorAggregator.addError(
        lineNumber,
        columnName,
        String.format("Valor '%s' não é um número válido", value)
      );
    }
  }
}
