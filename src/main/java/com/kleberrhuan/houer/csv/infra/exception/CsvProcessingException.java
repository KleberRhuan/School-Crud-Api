/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.exception;

import com.kleberrhuan.houer.common.infra.exception.InfrastructureException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/** Exceção lançada quando há problemas de infraestrutura no processamento do CSV. */
public class CsvProcessingException extends InfrastructureException {

  public CsvProcessingException(String message, Throwable cause) {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.SYSTEM_ERROR,
      MessageKey.of("error.csv.processing"),
      message
    );
    this.initCause(cause);
  }

  public CsvProcessingException(String message) {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.SYSTEM_ERROR,
      MessageKey.of("error.csv.processing"),
      message
    );
  }
}
