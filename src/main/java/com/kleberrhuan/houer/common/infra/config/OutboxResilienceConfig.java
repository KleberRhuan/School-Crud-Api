/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OutboxResilienceConfig {

  @Bean
  Clock systemClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  CircuitBreaker circuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("outbox-store");
  }
}
