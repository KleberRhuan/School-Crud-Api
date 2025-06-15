/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheFactory {

  @Getter
  private final Cache<String, Bucket> rateLimitCache;

  @Getter
  private final Cache<String, Boolean> jwtBlockList;
}
