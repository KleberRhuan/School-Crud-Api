/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TracingAspect {

  @Around(
    "(@within(org.springframework.stereotype.Service) || " +
    "@within(org.springframework.web.bind.annotation.RestController)) && " +
    "!@within(com.kleberrhuan.houer.common.infra.aspect.NoTracing)"
  )
  public Object profile(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    try {
      return pjp.proceed();
    } finally {
      long ms = (System.nanoTime() - start) / 1_000_000;
      log.debug("{} executed in {} ms", pjp.getSignature().toShortString(), ms);
    }
  }
}
