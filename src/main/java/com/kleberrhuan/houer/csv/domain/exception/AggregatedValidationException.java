/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import java.util.List;
import java.util.stream.Collectors;

public class AggregatedValidationException extends CsvValidationException {

  private final transient List<ValidationErrorAggregator.ValidationError> errors;

  @Override
  public String getMessage() {
    return buildMessage(errors);
  }

  public AggregatedValidationException(
    List<ValidationErrorAggregator.ValidationError> errors
  ) {
    super(buildMessage(errors));
    this.errors = List.copyOf(errors);
  }

  public List<ValidationErrorAggregator.ValidationError> getErrors() {
    return errors;
  }

  private static String buildMessage(
    List<ValidationErrorAggregator.ValidationError> errors
  ) {
    if (errors.isEmpty()) {
      return "Validação falhou";
    }

    String summary = String.format(
      "Validação falhou com %d erro(s)",
      errors.size()
    );
    String details = errors
      .stream()
      .map(ValidationErrorAggregator.ValidationError::toString)
      .collect(Collectors.joining("; "));

    return summary + ": " + details;
  }
}
