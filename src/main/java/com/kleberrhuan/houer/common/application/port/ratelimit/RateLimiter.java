/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.ratelimit;

import com.kleberrhuan.houer.common.infra.ratelimit.RateCheck;

@FunctionalInterface
public interface RateLimiter {
  RateCheck check(String key);
}
