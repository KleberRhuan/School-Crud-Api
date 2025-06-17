/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionLoggingAspect {

  @AfterThrowing(
    pointcut = "within(@org.springframework.web.bind.annotation.RestController *)",
    throwing = "ex"
  )
  @Counted(
    value = "app.errors.total",
    description = "Total de exceções não capturadas"
  )
  public void logAndCount(JoinPoint jp, Exception ex) {
    HttpStatus status = resolveStatus(ex);

    String where = jp.getSignature().toShortString();
    String msg = ex.getMessage();

    if (status.is4xxClientError()) {
      log.warn(
        "4xx error in {}: {} - {}",
        where,
        ex.getClass().getSimpleName(),
        msg
      );
    } else {
      log.error(
        "5xx error in {}: {} - {}",
        where,
        ex.getClass().getSimpleName(),
        msg,
        ex
      );
    }
  }

  /* ------------ helpers ----------- */
  private HttpStatus resolveStatus(Exception ex) {
    ResponseStatus ann = ex.getClass().getAnnotation(ResponseStatus.class);
    if (ann != null) return ann.code();
    return (ex instanceof ApiException api)
      ? api.getStatus()
      : HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
