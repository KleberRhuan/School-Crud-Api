/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kleberrhuan.houer.common.infra.properties.HouerProperties;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CaffeineCacheConfig {

  private final HouerProperties props;

  @Value("${school.cache.catalog.ttl:PT2H}")
  private Duration catalogTtl;

  @Value("${school.cache.page.ttl:PT30M}")
  private Duration pageTtl;

  @Value("${school.cache.page.max-size:1000}")
  private long pageMaxSize;

  @Bean
  @Primary
  public CacheManager cacheManager() {
    var mgr = new CaffeineCacheManager();
    mgr.setCaffeine(
        Caffeine
            .newBuilder()
            .maximumSize(props.getDefault().maxSize())
            .expireAfterWrite(props.getDefault().ttl())
            .recordStats());
    mgr.setAllowNullValues(false);
    return mgr;
  }

  @Bean("rateLimit")
  public Cache<String, Bucket> caffeineRateLimiter() {
    var s = props.get("rate-limit");
    return Caffeine
        .newBuilder()
        .maximumSize(s.maxSize())
        .expireAfterWrite(s.ttl())
        .recordStats()
        .build();
  }

  @Bean("jwtBlockList")
  public Cache<String, Boolean> jwtBlockListCache() {
    var s = props.get("jwt-block-list");
    return Caffeine
        .newBuilder()
        .maximumSize(s.maxSize())
        .expireAfterWrite(s.ttl())
        .recordStats()
        .build();
  }

  // ========== Caches para Controle de Concorrência ==========

  /**
   * Cache para idempotência de emails.
   * TTL de 24h para evitar reenvios acidentais.
   */
  @Bean("emailIdempotencyCache")
  public Cache<String, String> emailIdempotencyCache() {
    return Caffeine
        .newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(24, TimeUnit.HOURS)
        .recordStats()
        .build();
  }

  @Bean("emailRateLimitCache")
  public Cache<String, Integer> emailRateLimitCache() {
    return Caffeine
        .newBuilder()
        .maximumSize(5_000)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .recordStats()
        .build();
  }

  @Bean("metricCatalog")
  public CaffeineCache metricCatalogCache() {
    var catalogCaffeineBuilder = Caffeine
        .newBuilder()
        .expireAfterWrite(catalogTtl)
        .maximumSize(500)
        .recordStats();

    return new CaffeineCache("metricCatalog", catalogCaffeineBuilder.build());
  }

  @Bean("schoolsPage")
  public CaffeineCache schoolsPageCache() {
    var pageCaffeineBuilder = Caffeine
        .newBuilder()
        .expireAfterWrite(pageTtl)
        .maximumSize(pageMaxSize)
        .recordStats();

    return new CaffeineCache("schoolsPage", pageCaffeineBuilder.build());
  }

  @Bean
  public Caffeine<Object, Object> validMetricsCaffeine() {
    return Caffeine
        .newBuilder()
        .maximumSize(500)
        .expireAfterAccess(Duration.ofHours(12))
        .expireAfterWrite(Duration.ofDays(1))
        .recordStats();
  }
}
