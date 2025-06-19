/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import com.kleberrhuan.houer.common.domain.exception.BusinessException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/** Exceção lançada quando há problemas na validação do CSV de escolas. */
public class CsvValidationException extends BusinessException {

  public CsvValidationException(String message) {
    super(
      HttpStatus.UNPROCESSABLE_ENTITY,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("error.csv.validation"),
      message
    );
  }

  public CsvValidationException(String message, Object... args) {
    super(
      HttpStatus.UNPROCESSABLE_ENTITY,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("error.csv.validation"),
      args
    );
  }
}
