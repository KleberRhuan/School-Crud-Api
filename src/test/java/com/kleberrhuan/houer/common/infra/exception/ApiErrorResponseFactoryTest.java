/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorResponse;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;

class ApiErrorResponseFactoryTest {

  private ApiErrorResponseFactory factory;

  @BeforeEach
  void setup() {
    com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType.setBaseUrl(
      "https://api.test"
    );
    StaticMessageSource ms = new StaticMessageSource();
    ms.addMessage("err.message", Locale.ENGLISH, "User message");
    ms.addMessage("err.detail", Locale.ENGLISH, "Detail message {0}");
    ms.addMessage(
      "err.message",
      Locale.forLanguageTag("pt-BR"),
      "Mensagem usuário"
    );
    ms.addMessage("err.detail", Locale.forLanguageTag("pt-BR"), "Detalhe {0}");
    factory = new ApiErrorResponseFactory(ms);
  }

  @Test
  @DisplayName("deve construir resposta em pt-BR")
  void shouldBuildPtBr() {
    MessageKey key = MessageKey.of("err");
    ApiErrorResponse res = factory.build(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.BUSINESS_ERROR,
      key,
      Locale.forLanguageTag("pt-BR"),
      new Object[] { "X" }
    );

    assertThat(res.userMessage()).isEqualTo("Mensagem usuário");
    assertThat(res.detail()).isEqualTo("Detalhe X");
  }

  @Test
  @DisplayName("deve usar fallback quando tradução ausente")
  void shouldFallback() {
    MessageKey key = MessageKey.of("missing");
    ApiErrorResponse res = factory.build(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.BUSINESS_ERROR,
      key,
      Locale.ENGLISH,
      new Object[] {}
    );
    // detail fallback é base key
    assertThat(res.detail()).isEqualTo("missing");
  }
}
