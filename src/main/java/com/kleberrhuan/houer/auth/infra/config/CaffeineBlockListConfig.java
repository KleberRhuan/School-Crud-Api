/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kleberrhuan.houer.auth.infra.properties.JwtProps;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineBlockListConfig {

  @Bean("jwtBlockCache")
  Cache<String, Boolean> jwtBlockCache(JwtProps props) {
    return Caffeine
      .newBuilder()
      .expireAfterWrite(Duration.ofSeconds(props.accessTtlSec()))
      .maximumSize(100_000)
      .build();
  }
}
