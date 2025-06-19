/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator.rule;

import com.kleberrhuan.houer.csv.domain.exception.RowValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Regra específica para validação do código da escola (CODESC). Aplica validações especiais para este campo crítico.
 */
@Component
@Order(3) // Executada após validações básicas
@Slf4j
public final class SchoolCodeRule implements RowRule {

  @Override
  public void validate(RowContext context) {
    String[] headers = context.headers();
    String[] values = context.values();

    for (int i = 0; i < headers.length && i < values.length; i++) {
      String columnName = headers[i].trim();
      String value = values[i];

      if (CsvSchoolColumn.CODESC.name().equals(columnName)) {
        validateSchoolCode(value, context);
        break;
      }
    }
  }

  private void validateSchoolCode(String value, RowContext context) {
    if (value == null || value.trim().isEmpty()) {
      throw new RowValidationException(
        context.filename(),
        context.lineNumber(),
        "Código da escola (CODESC) é obrigatório"
      );
    }

    String trimmedValue = value.trim();

    //
    try {
      long code = Long.parseLong(trimmedValue);

      if (code <= 0) {
        throw new RowValidationException(
          context.filename(),
          context.lineNumber(),
          CsvSchoolColumn.CODESC.name(),
          trimmedValue,
          "Código da escola deve ser positivo"
        );
      }

      log.trace("Código da escola válido: {}", code);
    } catch (NumberFormatException e) {
      throw new RowValidationException(
        context.filename(),
        context.lineNumber(),
        CsvSchoolColumn.CODESC.name(),
        trimmedValue,
        "Código da escola deve ser um número inteiro válido"
      );
    }
  }
}
