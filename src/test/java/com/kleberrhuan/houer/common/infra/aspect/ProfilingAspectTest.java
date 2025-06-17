/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfilingAspectTest {

  @Test
  @DisplayName(
    "deve registrar timer e nao contar erro em execucao bem-sucedida"
  )
  void shouldRecordTimerOnSuccess() throws Throwable {
    SimpleMeterRegistry reg = new SimpleMeterRegistry();
    ProfilingAspect aspect = new ProfilingAspect(reg);

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenReturn("ok");

    Signature sig = mock(Signature.class);
    when(sig.getDeclaringTypeName()).thenReturn("MyService");
    when(sig.getName()).thenReturn("doStuff");
    when(pjp.getSignature()).thenReturn(sig);

    Object out = aspect.profile(pjp);
    assertThat(out).isEqualTo("ok");

    // timer registrado
    assertThat(reg.find("service.execution.time").timer()).isNotNull();
    // nenhum erro contado
    Counter err = reg.counter("service.errors.total");
    assertThat(err.count()).isZero();
  }

  @Test
  @DisplayName("deve incrementar contador de erros quando excecao lancada")
  void shouldIncrementErrorCounter() throws Throwable {
    SimpleMeterRegistry reg = new SimpleMeterRegistry();
    ProfilingAspect aspect = new ProfilingAspect(reg);

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenThrow(new IllegalStateException("boom"));

    Signature sig = mock(Signature.class);
    when(sig.getDeclaringTypeName()).thenReturn("MyService");
    when(sig.getName()).thenReturn("fail");
    when(pjp.getSignature()).thenReturn(sig);

    assertThatThrownBy(() -> aspect.profile(pjp))
      .isInstanceOf(IllegalStateException.class);

    Counter err = reg.counter("service.errors.total");
    assertThat(err.count()).isEqualTo(1);
  }
}
