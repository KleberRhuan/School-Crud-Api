/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception.factory;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorResponse;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiErrorResponseFactory {

  private final MessageSource messageSource;

  public ApiErrorResponse build(
    HttpStatus status,
    ApiErrorType type,
    MessageKey key,
    Locale locale,
    Object[] args
  ) {
    String detail = messageSource.getMessage(
      key.detailCode(),
      args,
      key.base(), // Fallback caso a mensagem detalhada não exista
      locale
    );

    // Resolve a mensagem amigável para usuários finais
    String userMessage = messageSource.getMessage(
      key.messageCode(),
      args,
      detail, // Fallback para a mensagem detalhada se a de usuário não existir
      locale
    );

    return ApiErrorResponse.of(type, status, detail, userMessage, null);
  }
}
