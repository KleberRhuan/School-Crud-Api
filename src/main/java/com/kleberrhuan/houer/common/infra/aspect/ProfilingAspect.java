/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ProfilingAspect {

  private final MeterRegistry registry;
  private final Counter errorCounter = Counter
    .builder("service.errors.total")
    .description("Total de exceções lançadas em serviços")
    .register(registry);

  @Around("@within(org.springframework.stereotype.Service)")
  public Object profile(ProceedingJoinPoint pjp) throws Throwable {
    String clazz = pjp.getSignature().getDeclaringTypeName();
    String method = pjp.getSignature().getName();

    Timer.Sample sample = Timer.start(registry);
    try {
      return pjp.proceed();
    } catch (Throwable t) {
      errorCounter.increment();
      throw t;
    } finally {
      sample.stop(
        Timer
          .builder("service.execution.time")
          .description("Tempo de execução de métodos de serviço")
          .tags("class", clazz, "method", method)
          .register(registry)
      );
    }
  }
}
