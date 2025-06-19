/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import com.kleberrhuan.houer.common.domain.exception.BusinessException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class ActiveImportJobException extends BusinessException {

  private final String detailMessage;

  @Override
  public String getMessage() {
    return detailMessage;
  }

  public ActiveImportJobException(String detailMessage) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("error.csv.import.job.active"),
      detailMessage
    );
    this.detailMessage = detailMessage;
  }
}
