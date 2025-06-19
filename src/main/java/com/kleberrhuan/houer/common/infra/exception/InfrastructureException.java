/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InfrastructureException extends ApiException {

  public InfrastructureException(
    @Nullable HttpStatus status,
    @Nullable ApiErrorType errorType,
    @Nullable MessageKey key,
    @Nullable Object... args
  ) {
    super(
      status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
      errorType != null ? errorType : ApiErrorType.SYSTEM_ERROR,
      key != null ? key : MessageKey.of("error.infrastructure.default"),
      args != null ? args : new Object[0]
    );
  }

  public static InfrastructureException defaultException(String message) {
    return new InfrastructureException(
      HttpStatus.INTERNAL_SERVER_ERROR,
      ApiErrorType.SYSTEM_ERROR,
      MessageKey.of("error.infrastructure.default"),
      message
    );
  }
}
