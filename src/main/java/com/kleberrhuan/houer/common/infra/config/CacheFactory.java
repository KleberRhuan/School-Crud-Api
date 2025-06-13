/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheFactory {

  private final Map<String, Cache<String, AtomicInteger>> caches;
  private final Map<String, Cache<String, Instant>> instantCaches;

  public Cache<String, AtomicInteger> getMinuteCache() {
    return caches.get("rate-minute");
  }

  public Cache<String, AtomicInteger> getAuthCache() {
    return caches.get("rate-auth");
  }

  public Cache<String, Instant> getJwtBlacklist() {
    return instantCaches.get("jwt-blacklist");
  }
}
