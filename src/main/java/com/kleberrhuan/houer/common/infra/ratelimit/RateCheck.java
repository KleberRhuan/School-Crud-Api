/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit;

public record RateCheck(
  boolean exceeded,
  long retryAfterSeconds,
  long remainingPermits
) {}
