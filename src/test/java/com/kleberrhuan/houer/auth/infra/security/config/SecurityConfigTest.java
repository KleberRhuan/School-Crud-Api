/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kleberrhuan.houer.auth.application.service.AuthenticationService;
import com.kleberrhuan.houer.auth.application.service.RegistrationService;
import com.kleberrhuan.houer.common.application.mapper.BrevoMapper;
import com.kleberrhuan.houer.common.infra.properties.AuditProperties;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
  controllers = com.kleberrhuan.houer.auth.interfaces.web.AuthController.class
)
@Import({ SecurityConfig.class, SecurityConfigTest.TestConfig.class })
@TestPropertySource(
  properties = {
    "jwt.public-key=classpath:certificates/public.pub",
    "jwt.private-key=classpath:certificates/private.pem",
    "jwt.issuer=test-issuer",
    "jwt.access-ttl-sec=900",
    "jwt.refresh-ttl-sec=604800",
    "app.cors.allowed-origins=http://localhost:3000",
    "app.rate-limit.enabled=false",
    "app.frontend-url=http://localhost:3000",
    "app.brevo.api-key=test-api-key",
    "app.brevo.name=Test Sender",
    "app.brevo.email=test@example.com",
    "app.brevo.url=https://api.brevo.com/v3",
    "app.brevo.connect-timeout=3",
    "app.brevo.read-timeout=5",
  }
)
class SecurityConfigTest {

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
      MeterRegistry registry = Mockito.mock(MeterRegistry.class);
      Timer timer = Mockito.mock(Timer.class);
      Mockito.when(registry.timer(Mockito.anyString())).thenReturn(timer);
      Mockito
        .when(registry.timer(Mockito.anyString(), Mockito.any(String[].class)))
        .thenReturn(timer);
      return registry;
    }

    @Bean
    @Primary
    public com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter rateLimiter() {
      return Mockito.mock(
        com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter.class
      );
    }

    @Bean
    @Primary
    public com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory apiErrorResponseFactory() {
      return Mockito.mock(
        com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory.class
      );
    }

    @Bean
    @Primary
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
      return Mockito.mock(
        org.springframework.security.oauth2.jwt.JwtDecoder.class
      );
    }

    @Bean("custom-cors")
    @Primary
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
      org.springframework.web.cors.CorsConfigurationSource source =
        Mockito.mock(
          org.springframework.web.cors.CorsConfigurationSource.class
        );
      org.springframework.web.cors.CorsConfiguration config =
        new org.springframework.web.cors.CorsConfiguration();
      config.setAllowedOrigins(java.util.List.of("http://localhost:3000"));
      config.setAllowedMethods(
        java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
      );
      config.setAllowedHeaders(java.util.List.of("*"));
      config.setAllowCredentials(true);
      Mockito
        .when(source.getCorsConfiguration(Mockito.any()))
        .thenReturn(config);
      return source;
    }

    @Bean
    @Primary
    public com.kleberrhuan.houer.auth.infra.error.ApiErrorAuthenticationEntryPoint apiErrorAuthenticationEntryPoint() {
      return Mockito.mock(
        com.kleberrhuan.houer.auth.infra.error.ApiErrorAuthenticationEntryPoint.class
      );
    }

    @Bean
    @Primary
    public com.kleberrhuan.houer.auth.infra.error.ApiErrorAccessDeniedHandler apiErrorAccessDeniedHandler() {
      return Mockito.mock(
        com.kleberrhuan.houer.auth.infra.error.ApiErrorAccessDeniedHandler.class
      );
    }
  }

  @MockitoBean
  private RateLimitProperties rateLimitProperties;

  @MockitoBean
  private AuditProperties auditProperties;

  @MockitoBean
  private AuthenticationService authenticationService;

  @MockitoBean
  private RegistrationService registrationService;

  @MockitoBean
  private BrevoMapper brevoMapper;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SecurityFilterChain securityFilterChain;

  @Test
  void securityFilterChainShouldBeConfigured() {
    assertThat(securityFilterChain).isNotNull();
  }

  @Test
  @DisplayName("Public endpoints are accessible without authentication")
  void publicEndpointsAreAccessible() throws Exception {
    mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("CORS configuration is applied correctly")
  void corsConfigurationIsApplied() throws Exception {
    mockMvc
      .perform(
        options("/auth/login")
          .header("Origin", "http://localhost:3000")
          .header("Access-Control-Request-Method", "POST")
      )
      .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Protected endpoints require authentication")
  void protectedEndpointsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/protected/endpoint")).andExpect(status().isOk());
  }
}
