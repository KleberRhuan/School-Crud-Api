/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.web.filter;

import com.kleberrhuan.houer.common.infra.properties.AuditProperties;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.pattern.PathPatternParser;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

  private final MeterRegistry registry;
  private final AuditProperties cfg;
  private static final PathPatternParser PARSER =
    PathPatternParser.defaultInstance;

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest req,
    @NonNull HttpServletResponse res,
    @NonNull FilterChain chain
  ) throws ServletException, IOException {
    /* Ignora paths configurados ------------------------- */
    boolean ignore = cfg
      .getIgnoredPaths()
      .stream()
      .map(PARSER::parse)
      .anyMatch(p -> p.matches(PathContainer.parsePath(req.getRequestURI())));
    if (ignore) {
      chain.doFilter(req, res);
      return;
    }

    /* Envolve wrappers só se corpo será logado ---------- */
    boolean needsBody = cfg.isLogRequestBody() || cfg.isLogResponseBody();
    HttpServletRequest r = needsBody
      ? new ContentCachingRequestWrapper(req)
      : req;
    HttpServletResponse p = needsBody
      ? new ContentCachingResponseWrapper(res)
      : res;

    long startNanos = System.nanoTime();
    Instant startTs = Instant.now();

    try {
      if (cfg.isLogRequests()) logRequest(r, startTs);
      chain.doFilter(r, p);
    } finally {
      long elapsedMs = TimeUnit.NANOSECONDS.toMillis(
        System.nanoTime() - startNanos
      );

      registry
        .timer(
          "http.custom.latency",
          "path",
          req.getRequestURI(),
          "status",
          String.valueOf(p.getStatus())
        )
        .record(elapsedMs, TimeUnit.MILLISECONDS);

      if (cfg.isLogResponses()) logResponse(r, p, elapsedMs);
      if (needsBody) ((ContentCachingResponseWrapper) p).copyBodyToResponse();
    }
  }

  /* ---------- helpers ---------------------------------- */

  private void logRequest(HttpServletRequest r, Instant ts) {
    Map<String, String> headers = Collections
      .list(r.getHeaderNames()) // Enumeration -> List
      .stream()
      .collect(
        Collectors.toMap(
          h -> h,
          h ->
            cfg
                .getSensitiveHeaders()
                .stream()
                .anyMatch(s -> s.equalsIgnoreCase(h))
              ? "***MASKED***"
              : r.getHeader(h)
        )
      );

    String body = cfg.isLogRequestBody()
      ? bodyString((ContentCachingRequestWrapper) r, "[EMPTY]")
      : "[REQUEST BODY LOGGING DISABLED]";

    log.info(
      "→ [{}] {} {} | headers={} | body={}",
      ts,
      r.getMethod(),
      r.getRequestURI(),
      headers,
      body
    );
  }

  private void logResponse(
    HttpServletRequest r,
    HttpServletResponse p,
    long ms
  ) {
    if (cfg.isLogOnlyFailures() && p.getStatus() < 400) return;

    String body = cfg.isLogResponseBody()
      ? bodyString((ContentCachingResponseWrapper) p, "[EMPTY]")
      : "[RESPONSE BODY LOGGING DISABLED]";

    log.info(
      "← {} {} | status={} | {} ms | body={}",
      r.getMethod(),
      r.getRequestURI(),
      p.getStatus(),
      ms,
      body
    );
  }

  /* Conversão de corpo ---------------------------------- */

  private String bodyString(ContentCachingRequestWrapper w, String empty) {
    return formatBody(w.getContentAsByteArray(), empty);
  }

  private String bodyString(ContentCachingResponseWrapper w, String empty) {
    return formatBody(w.getContentAsByteArray(), empty);
  }

  private String formatBody(byte[] bytes, String empty) {
    if (bytes.length == 0) return empty;
    if (bytes.length > cfg.getMaxBodySize()) return "[BODY TOO LARGE]";
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
