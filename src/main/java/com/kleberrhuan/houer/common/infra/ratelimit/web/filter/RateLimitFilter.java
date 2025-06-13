/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import com.kleberrhuan.houer.common.infra.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitProperties props;
  private final RateLimiter limiter;
  private final ObjectMapper mapper;
  private final ApiErrorResponseFactory errorFactory;

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest req,
    @NonNull HttpServletResponse res,
    @NonNull FilterChain chain
  ) throws ServletException, IOException {
    if (
      !props.isEnabled() ||
      !limiter.exceeded(clientIp(req), req.getRequestURI())
    ) {
      chain.doFilter(req, res);
      return;
    }
    handleTooManyRequests(res);
  }

  /* helpers ------------------------------------------------------- */

  private static String clientIp(HttpServletRequest r) {
    String header = Optional
      .ofNullable(r.getHeader("X-Forwarded-For"))
      .orElse(r.getHeader("X-Real-IP"));
    return (header == null || header.isBlank())
      ? r.getRemoteAddr()
      : header.split(",")[0].trim();
  }

  private void handleTooManyRequests(HttpServletResponse res)
    throws IOException {
    res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    res.setHeader("Retry-After", "60");
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    var payload = errorFactory.build(
      HttpStatus.TOO_MANY_REQUESTS,
      ApiErrorType.RATE_LIMIT_EXCEEDED,
      MessageKey.of("error.infrastructure.rate-limit.exceeded"),
      Locale.getDefault(),
      new Object[0]
    );
    mapper.writeValue(res.getOutputStream(), payload);
  }
}
