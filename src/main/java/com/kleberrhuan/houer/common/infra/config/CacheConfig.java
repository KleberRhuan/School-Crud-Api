/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kleberrhuan.houer.common.infra.properties.CacheSpecsProperties;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private final CacheSpecsProperties props;

  @Bean
  @Primary
  public CacheManager cacheManager() {
    var mgr = new CaffeineCacheManager();
    mgr.setCaffeine(
      Caffeine
        .newBuilder()
        .maximumSize(props.getDefault().getMaxSize())
        .expireAfterWrite(props.getDefault().getTtl())
        .recordStats()
    );
    mgr.setAllowNullValues(false);
    return mgr;
  }

  @Bean("jwtBlacklist")
  public Cache<String, Instant> jwtBlacklist() {
    var s = props.get("jwt-blacklist");
    return Caffeine
      .newBuilder()
      .maximumSize(s.getMaxSize())
      .expireAfterWrite(s.getTtl())
      .recordStats()
      .build();
  }

  @Bean("rateMinute")
  public Cache<String, AtomicInteger> rateMinute() {
    var s = props.get("rate-minute");
    return Caffeine
      .newBuilder()
      .maximumSize(s.getMaxSize())
      .expireAfterWrite(s.getTtl())
      .build();
  }

  @Bean("rateAuth")
  public Cache<String, AtomicInteger> authRateMinute() {
    var s = props.get("rate-auth");
    return Caffeine
      .newBuilder()
      .maximumSize(s.getMaxSize())
      .expireAfterWrite(s.getTtl())
      .build();
  }
}
