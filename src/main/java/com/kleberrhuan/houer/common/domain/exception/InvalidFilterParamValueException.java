/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class InvalidFilterParamValueException extends BusinessException {

  public InvalidFilterParamValueException(String value) {
    super(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.INVALID_FILTER_PARAM_VALUE,
      MessageKey.of("error.business.invalidFilterParamValue"),
      value
    );
  }
}
