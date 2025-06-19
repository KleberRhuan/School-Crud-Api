/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import com.kleberrhuan.houer.common.domain.exception.BusinessException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/** Exceção específica para problemas na validação de linhas do CSV. */
public class RowValidationException extends BusinessException {

  private final String detailMessage;

  @Override
  public String getMessage() {
    return detailMessage;
  }

  public RowValidationException(
    String filename,
    int lineNumber,
    String message
  ) {
    super(
      HttpStatus.UNPROCESSABLE_ENTITY,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("error.csv.row.validation"),
      String.format("Linha %d do arquivo %s: %s", lineNumber, filename, message)
    );
    this.detailMessage =
      String.format(
        "Linha %d do arquivo %s: %s",
        lineNumber,
        filename,
        message
      );
  }

  public RowValidationException(
    String filename,
    int lineNumber,
    String fieldName,
    String value,
    String reason
  ) {
    super(
      HttpStatus.UNPROCESSABLE_ENTITY,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("error.csv.row.field"),
      String.format(
        "Campo '%s' com valor '%s' na linha %d do arquivo %s: %s",
        fieldName,
        value,
        lineNumber,
        filename,
        reason
      )
    );
    this.detailMessage =
      String.format(
        "Campo '%s' com valor '%s' na linha %d do arquivo %s: %s",
        fieldName,
        value,
        lineNumber,
        filename,
        reason
      );
  }
}
