/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ExceptionLoggingAspectTest {

  private ExceptionLoggingAspect aspect;
  private JoinPoint jp;

  @BeforeEach
  void setup() {
    aspect = new ExceptionLoggingAspect();

    jp = mock(JoinPoint.class);
    Signature sig = mock(Signature.class);
    when(sig.toShortString()).thenReturn("Controller.doIt()");
    when(jp.getSignature()).thenReturn(sig);
  }

  @Test
  @DisplayName("deve processar exceção sem erro")
  void shouldProcessException() {
    Exception ex = new IllegalArgumentException("bad");

    assertThatCode(() -> aspect.logAndCount(jp, ex)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("deve processar ApiException sem erro")
  void shouldProcessApiException() {
    ApiException ex = new ApiException(
      HttpStatus.NOT_FOUND,
      ApiErrorType.BUSINESS_ERROR,
      MessageKey.of("err.not.found")
    ) {};

    assertThatCode(() -> aspect.logAndCount(jp, ex)).doesNotThrowAnyException();
  }
}
