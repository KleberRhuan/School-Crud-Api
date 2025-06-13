/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception.constraint;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class NotNullConstraintViolationException
  extends ConstraintViolationException {

  public NotNullConstraintViolationException(String column) {
    super(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("error.business.constraint.notNull"),
      column
    );
  }
}
