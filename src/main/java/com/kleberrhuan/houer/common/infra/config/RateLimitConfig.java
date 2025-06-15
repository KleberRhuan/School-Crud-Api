/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.kleberrhuan.houer.common.application.port.ratelimit.RateLimiter;
import com.kleberrhuan.houer.common.infra.ratelimit.RateCheck;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class RateLimitConfig {

  private static final Bandwidth PUBLIC_RULE = Bandwidth
    .builder()
    .capacity(5)
    .refillGreedy(5, Duration.ofMinutes(1))
    .build();

  private static final Bandwidth AUTH_RULE = Bandwidth
    .builder()
    .capacity(60)
    .refillGreedy(60, Duration.ofMinutes(1))
    .build();

  @Bean
  public RateLimiter rateLimiter(CacheFactory factory) {
    Cache<String, Bucket> cache = factory.getRateLimitCache();

    RateLimiter publicLimiter = buildLimiter(cache, PUBLIC_RULE, "PUB:");
    RateLimiter authLimiter = buildLimiter(cache, AUTH_RULE, "AUTH:");

    return key -> {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAuth =
        auth != null &&
        auth.isAuthenticated() &&
        !(auth instanceof AnonymousAuthenticationToken);

      return isAuth ? authLimiter.check(key) : publicLimiter.check(key);
    };
  }

  private static RateLimiter buildLimiter(
    Cache<String, Bucket> cache,
    Bandwidth rule,
    String prefix
  ) {
    return key -> {
      Bucket bucket = cache.get(
        prefix + key,
        k -> Bucket.builder().addLimit(rule).build()
      );

      assert bucket != null;
      ConsumptionProbe p = bucket.tryConsumeAndReturnRemaining(1);
      return new RateCheck(
        !p.isConsumed(),
        TimeUnit.NANOSECONDS.toSeconds(p.getNanosToWaitForRefill()),
        p.getRemainingTokens()
      );
    };
  }
}
