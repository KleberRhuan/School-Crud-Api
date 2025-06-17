/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TracingAspectTest {

  @Test
  @DisplayName("deve delegar chamada e retornar valor do proceed")
  void shouldProceedAndReturn() throws Throwable {
    TracingAspect aspect = new TracingAspect();
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenReturn(42);

    Signature sig = mock(Signature.class);
    when(sig.toShortString()).thenReturn("Svc.method()");
    when(pjp.getSignature()).thenReturn(sig);

    Object out = aspect.profile(pjp);
    assertThat(out).isEqualTo(42);
    verify(pjp).proceed();
  }

  @Test
  @DisplayName("deve propagar excecao lançada pelo método")
  void shouldPropagateException() throws Throwable {
    TracingAspect aspect = new TracingAspect();
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenThrow(new IllegalStateException("boom"));

    Signature sig = mock(Signature.class);
    when(sig.toShortString()).thenReturn("Svc.fail()");
    when(pjp.getSignature()).thenReturn(sig);

    assertThatThrownBy(() -> aspect.profile(pjp))
      .isInstanceOf(IllegalStateException.class);
  }
}
