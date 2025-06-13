/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception.constraint;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class ConstraintViolationException extends ApiException {

  public ConstraintViolationException(
    HttpStatus status,
    ApiErrorType errorType,
    MessageKey messageKey,
    Object... args
  ) {
    super(status, errorType, messageKey, args);
  }
}
