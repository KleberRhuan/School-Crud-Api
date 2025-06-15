/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.observability;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class PostValidationFilter extends OncePerRequestFilter {

  private final MeterRegistry meter;

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
        meter.counter("auth.request.ok").increment();
      }
      chain.doFilter(req, res);
    } finally {
      MDC.remove("token_jti");
    }
  }
}
