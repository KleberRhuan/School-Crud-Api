/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

  public ForbiddenException() {
    super(
      HttpStatus.FORBIDDEN,
      ApiErrorType.FORBIDDEN,
      MessageKey.of("error.business.security.forbidden")
    );
  }
}
