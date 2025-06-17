/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit.web.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import com.kleberrhuan.houer.common.infra.ratelimit.RateCheck;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  private RateLimiter limiter;
  private RateLimitFilter filter;

  @BeforeEach
  void setup() {
    RateLimitProperties props = new RateLimitProperties();
    props.setRequestsPerMinute(10);
    props.setAuthRequestsPerMinute(5);
    props.setEnabled(true);

    limiter = mock(RateLimiter.class);
    ObjectMapper mapper = new ObjectMapper();
    ApiErrorResponseFactory erf = mock(ApiErrorResponseFactory.class);
    filter = new RateLimitFilter(props, limiter, mapper, erf);
  }

  @Test
  @DisplayName("deve permitir requisições quando limte não excedido")
  void shouldAllowWhenNotExceeded() throws Exception {
    when(limiter.check(any())).thenReturn(new RateCheck(false, 0, 3));
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = spy(new MockFilterChain());

    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
    assertThat(res.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(res.getHeader("X-RateLimit-Remaining")).isEqualTo("3");
  }

  @Test
  @DisplayName("deve bloquear e retornar 429 quando limite excedido")
  void shouldBlockWhenExceeded() throws Exception {
    when(limiter.check(any())).thenReturn(new RateCheck(true, 30, 0));
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilterInternal(req, res, chain);

    verify(chain, never()).doFilter(any(), any());
    assertThat(res.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    assertThat(res.getHeader("Retry-After")).isEqualTo("30");
  }
}
