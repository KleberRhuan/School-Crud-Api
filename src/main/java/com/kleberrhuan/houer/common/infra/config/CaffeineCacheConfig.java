/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kleberrhuan.houer.common.infra.properties.HouerProperties;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@ConditionalOnProperty(
  name = "auth.cache.provider",
  havingValue = "caffeine",
  matchIfMissing = true
)
public class CaffeineCacheConfig {

  private final HouerProperties props;

  @Bean
  @Primary
  public CacheManager cacheManager() {
    var mgr = new CaffeineCacheManager();
    mgr.setCaffeine(
      Caffeine
        .newBuilder()
        .maximumSize(props.getDefault().maxSize())
        .expireAfterWrite(props.getDefault().ttl())
        .recordStats()
    );
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
}
