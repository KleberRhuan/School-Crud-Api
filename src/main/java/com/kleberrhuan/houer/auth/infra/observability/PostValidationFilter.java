/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostValidationFilter extends OncePerRequestFilter {

  private final MeterRegistry meterRegistry;
  private Counter authRequestOkCounter;

  @Override
  public void afterPropertiesSet() {
    this.authRequestOkCounter = meterRegistry.counter("auth.request.ok");
  }

  @Override
  protected void doFilterInternal(
    @NotNull HttpServletRequest req,
    @NotNull HttpServletResponse res,
    @NotNull FilterChain chain
  ) throws ServletException, java.io.IOException {
    try {
      var auth = (JwtAuthenticationToken) SecurityContextHolder
        .getContext()
        .getAuthentication();

      if (auth != null) {
        String jti = auth.getToken().getId();
        MDC.put("token_jti", jti);
        log.info("Authenticated request: jti={}", jti);
        authRequestOkCounter.increment();
      }
      chain.doFilter(req, res);
    } finally {
      MDC.remove("token_jti");
    }
  }
}
