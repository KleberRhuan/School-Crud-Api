/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BusinessException {

  public EntityNotFoundException(String entity, Object id) {
    super(
      HttpStatus.NOT_FOUND,
      ApiErrorType.RESOURCE_NOT_FOUND,
      MessageKey.of("error.business.entity.notFound"),
      entity,
      id
    );
  }
}
