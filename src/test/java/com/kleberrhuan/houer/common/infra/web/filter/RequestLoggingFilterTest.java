/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.web.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.infra.properties.AuditProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

  private RequestLoggingFilter filter;
  private AuditProperties auditProps;

  @BeforeEach
  void setup() {
    auditProps = new AuditProperties();
    auditProps.setLogRequests(true);
    auditProps.setLogResponses(true);
    auditProps.setLogRequestBody(true);
    auditProps.setLogResponseBody(true);
    auditProps.setIgnoredPaths(java.util.List.of("/ignore/**"));

    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    filter = new RequestLoggingFilter(auditProps, registry);
    filter.afterPropertiesSet();
  }

  @Test
  @DisplayName("deve ignorar path configurado")
  void shouldSkipIgnoredPath() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest(
      "GET",
      "/ignore/health"
    );
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = spy(new MockFilterChain());

    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  @DisplayName("deve processar requisição normal")
  void shouldProcessNormalRequest() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest(
      "POST",
      "/api/test"
    );
    req.setContent("hello".getBytes());
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = (r, p) -> {
      ((jakarta.servlet.http.HttpServletResponse) p).setStatus(
          HttpStatus.OK.value()
        );
      p.getWriter().write("OK");
    };

    assertThatCode(() -> filter.doFilterInternal(req, res, chain))
      .doesNotThrowAnyException();

    assertThat(res.getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
