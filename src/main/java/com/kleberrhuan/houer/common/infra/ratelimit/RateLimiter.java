/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit;

import com.kleberrhuan.houer.common.infra.config.CacheFactory;
import com.kleberrhuan.houer.common.infra.properties.RateLimitProperties;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimiter {

  private final RateLimitProperties props;
  private final CacheFactory cacheFactory;

  private final Map<LimitGroup, LimitRule> rules = Map.of(
    LimitGroup.AUTH,
    new LimitRule(
      props.getAuthRequestsPerMinute(),
      Duration.ofMinutes(1),
      cacheFactory.getAuthCache()
    ),
    LimitGroup.GENERAL,
    new LimitRule(
      props.getRequestsPerMinute(),
      Duration.ofMinutes(1),
      cacheFactory.getMinuteCache()
    )
  );

  public boolean exceeded(String clientIp, String uri) {
    LimitGroup group = rules
      .keySet()
      .stream()
      .filter(g -> g.matches(uri))
      .findFirst()
      .orElse(LimitGroup.GENERAL);

    LimitRule rule = rules.get(group);
    String key = group.name() + ":" + clientIp;
    return rule.exceeded(key);
  }
}
