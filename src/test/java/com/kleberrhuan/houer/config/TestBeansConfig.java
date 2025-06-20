/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.infra.config.CacheFactory;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.infra.properties.AuditProperties;
import com.kleberrhuan.houer.common.infra.properties.HouerProperties;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import com.kleberrhuan.houer.common.infra.properties.Spec;
import com.kleberrhuan.houer.common.infra.ratelimit.RateCheck;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestBeansConfig {

  @Bean
  @Primary
  public RateLimitProperties testRateLimitProperties() {
    RateLimitProperties props = new RateLimitProperties();
    props.setEnabled(false); // Desabilitado para testes
    props.setRequestsPerMinute(100);
    props.setAuthRequestsPerMinute(50);
    props.setTimeWindowMinutes(1);
    props.setRequestsPerHour(1000);
    props.setTimeWindowHours(1);
    return props;
  }

  @Bean
  @Primary
  public HouerProperties testHouerProperties() {
    HouerProperties props = new HouerProperties();
    props.setUrl("http://localhost:8080");

    // Configurar caches
    Map<String, Spec> caches = Map.of(
      "default",
      new Spec(Duration.ofMinutes(30), 1000L),
      "rate-limit",
      new Spec(Duration.ofMinutes(1), 1000L),
      "jwt-blacklist",
      new Spec(Duration.ofMinutes(15), 1000L)
    );
    props.setCaches(caches);

    return props;
  }

  @Bean
  @Primary
  public AuditProperties testAuditProperties() {
    AuditProperties props = new AuditProperties();
    props.setEnabled(true);
    props.setLogRequests(false); // Reduzir logs durante testes
    props.setLogResponses(false);
    props.setLogRequestBody(false);
    props.setLogResponseBody(false);
    return props;
  }

  @Bean
  @Primary
  public Cache<String, Bucket> testRateLimitCache() {
    return Caffeine
      .newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofMinutes(1))
      .build();
  }

  @Bean
  @Primary
  public Cache<String, Boolean> testJwtBlockListCache() {
    return Caffeine
      .newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofMinutes(15))
      .build();
  }

  @Bean
  @Primary
  public CacheFactory testCacheFactory() {
    return new CacheFactory(testRateLimitCache(), testJwtBlockListCache());
  }

  @Bean
  @Primary
  public RateLimiter testRateLimiter() {
    // Mock simples que sempre permite requisições para testes
    return key -> new RateCheck(false, 0, 100);
  }

  @Bean
  @Primary
  public ApiErrorResponseFactory testApiErrorResponseFactory() {
    // Retorna um mock simples para testes
    return org.mockito.Mockito.mock(ApiErrorResponseFactory.class);
  }

  @Bean
  @Primary
  public MeterRegistry testMeterRegistry() {
    // Mock do MeterRegistry para testes
    MeterRegistry registry = org.mockito.Mockito.mock(MeterRegistry.class);
    Timer timer = org.mockito.Mockito.mock(Timer.class);
    org.mockito.Mockito
      .when(registry.timer(org.mockito.Mockito.anyString()))
      .thenReturn(timer);
    org.mockito.Mockito
      .when(
        registry.timer(
          org.mockito.Mockito.anyString(),
          org.mockito.Mockito.any(String[].class)
        )
      )
      .thenReturn(timer);
    return registry;
  }
}
