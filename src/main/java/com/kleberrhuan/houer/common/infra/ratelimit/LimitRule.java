/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public record LimitRule(
  int maxRequests,
  Duration window,
  Cache<String, AtomicInteger> cache
) {
  boolean exceeded(String key) {
    AtomicInteger counter = cache.get(key, k -> new AtomicInteger());
    assert counter != null;
    return counter.incrementAndGet() > maxRequests;
  }
}
