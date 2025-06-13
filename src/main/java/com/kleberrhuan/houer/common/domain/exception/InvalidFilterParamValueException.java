/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class InvalidFilterParamValueException extends BusinessException {

  public InvalidFilterParamValueException(String value) {
    super(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.INVALID_FILTER_PARAM_VALUE,
      MessageKey.of("error.invalid_filter_param_value"),
      value
    );
  }
}
