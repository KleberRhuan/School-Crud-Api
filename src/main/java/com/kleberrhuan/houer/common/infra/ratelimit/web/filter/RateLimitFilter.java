/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import com.kleberrhuan.houer.common.infra.ratelimit.RateCheck;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @NotNull HttpServletRequest req,
    @NotNull HttpServletResponse res,
    @NotNull FilterChain chain
  ) throws ServletException, IOException {
    if (!props.isEnabled()) {
      chain.doFilter(req, res);
      return;
    }

    String key = clientIp(req);
    RateCheck rc = limiter.check(key);

    res.setHeader(
      "X-RateLimit-Limit",
      String.valueOf(
        isUserAuthenticated()
          ? props.getAuthRequestsPerMinute()
          : props.getRequestsPerMinute()
      )
    );
    res.setHeader(
      "X-RateLimit-Remaining",
      String.valueOf(rc.remainingPermits())
    );

    if (rc.exceeded()) {
      handleTooMany(req, res, rc.retryAfterSeconds());
      return;
    }

    chain.doFilter(req, res);
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

  private boolean isUserAuthenticated() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return (
      auth != null &&
      auth.isAuthenticated() &&
      !(auth instanceof AnonymousAuthenticationToken)
    );
  }

  private void handleTooMany(
    HttpServletRequest req,
    HttpServletResponse res,
    long retryAfter
  ) throws IOException {
    res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    res.setHeader("Retry-After", String.valueOf(retryAfter));

    var body = errorFactory.build(
      HttpStatus.TOO_MANY_REQUESTS,
      ApiErrorType.RATE_LIMIT_EXCEEDED,
      MessageKey.of("error.infrastructure.rate-limit.exceeded"),
      req.getLocale(),
      new Object[] { retryAfter }
    );

    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(res.getOutputStream(), body);
  }
}
