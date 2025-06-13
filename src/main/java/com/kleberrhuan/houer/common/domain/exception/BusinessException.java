/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

  public BusinessException(
    HttpStatus status,
    ApiErrorType errorType,
    MessageKey key,
    Object... args
  ) {
    super(status, errorType, key, args);
  }
}
