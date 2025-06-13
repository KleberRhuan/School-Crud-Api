/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

public class ProviderNotFoundException extends InfrastructureException {

  public ProviderNotFoundException(Provider provider) {
    super(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.INVALID_PROVIDER,
      MessageKey.of("error.infrastructure.notification.provider.notFound"),
      provider.name()
    );
  }
}
