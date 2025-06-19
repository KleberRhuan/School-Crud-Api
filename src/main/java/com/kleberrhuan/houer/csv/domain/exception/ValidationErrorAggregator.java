/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ValidationErrorAggregator {

  private final List<ValidationError> errors = new ArrayList<>();

  public void addError(int lineNumber, String columnName, String errorMessage) {
    errors.add(new ValidationError(lineNumber, columnName, errorMessage));
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public List<ValidationError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public void throwIfErrors() {
    if (hasErrors()) {
      throw new AggregatedValidationException(errors);
    }
  }

  public record ValidationError(
    int lineNumber,
    String columnName,
    String message
  ) {
    @Override
    public @NotNull String toString() {
      return String.format(
        "Linha %d, Coluna '%s': %s",
        lineNumber,
        columnName,
        message
      );
    }
  }
}
