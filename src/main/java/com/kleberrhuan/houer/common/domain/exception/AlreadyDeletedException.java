/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class AlreadyDeletedException extends BusinessException {

  public AlreadyDeletedException(String item, String id) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.ALREADY_DELETED,
      MessageKey.of("error.business.entity.alreadyDeleted"),
      item,
      id
    );
  }
}
