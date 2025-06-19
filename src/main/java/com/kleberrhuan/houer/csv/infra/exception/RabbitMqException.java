/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.exception;

import com.kleberrhuan.houer.common.infra.exception.InfrastructureException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class RabbitMqException extends InfrastructureException {

  public RabbitMqException(String message) {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.SYSTEM_ERROR,
      MessageKey.of("error.rabbitmq"),
      message
    );
  }
}
