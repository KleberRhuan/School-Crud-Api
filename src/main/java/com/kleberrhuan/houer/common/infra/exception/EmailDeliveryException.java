/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class EmailDeliveryException extends InfrastructureException {

  public EmailDeliveryException(Provider provider) {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.EMAIL_DELIVERY_ERROR,
      MessageKey.of("error.infrastructure.notification.email.delivery"),
      provider.name()
    );
  }
}
