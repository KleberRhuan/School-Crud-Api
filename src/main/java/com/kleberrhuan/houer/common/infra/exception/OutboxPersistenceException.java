/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class OutboxPersistenceException extends InfrastructureException {

  public OutboxPersistenceException() {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.OUTBOX_PERSISTENCE_ERROR,
      MessageKey.of("error.infrastructure.notification.outbox.persistence")
    );
  }
}
