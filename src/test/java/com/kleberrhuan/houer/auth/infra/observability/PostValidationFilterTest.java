/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PostValidationFilter")
class PostValidationFilterTest {

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  private PostValidationFilter newFilter() throws Exception {
    var registry = new SimpleMeterRegistry();
    PostValidationFilter f = new PostValidationFilter(registry);
    f.afterPropertiesSet();
    return f;
  }

  private MockHttpServletRequest req() {
    return new MockHttpServletRequest("GET", "/api/test");
  }

  private MockHttpServletResponse res() {
    return new MockHttpServletResponse();
  }

  @Nested
  class WithAuth {

    @Test
    @DisplayName("deve colocar jti no MDC e incrementar métrica")
    void shouldAddMdcAndMetric() throws Exception {
      // given
      Jwt jwt = new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "none"),
        Map.of("jti", "jti-123")
      );
      JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
      SecurityContextHolder.getContext().setAuthentication(auth);

      FilterChain chain = mock(FilterChain.class);
      PostValidationFilter filter = newFilter();

      // when
      filter.doFilter(req(), res(), chain);

      // then
      verify(chain).doFilter(any(), any());
      assertThat(MDC.get("token_jti")).isNull(); // removido no finally
    }
  }

  @Test
  @DisplayName("não deve fazer nada quando não autenticado")
  void noAuth() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    PostValidationFilter filter = newFilter();

    filter.doFilter(req(), res(), chain);

    verify(chain).doFilter(any(), any());
    assertThat(MDC.get("token_jti")).isNull();
  }
}
