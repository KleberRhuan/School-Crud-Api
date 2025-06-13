/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("outbox")
public record OutboxResilienceProperties(
  Duration backoff,
  int inMemoryMaxSize,
  int dueQueueWarningThreshold
) {
  public static final Duration DEFAULT_BACKOFF = Duration.ofMinutes(1);
  public static final int DEFAULT_MAX_SIZE = 10_000;
  public static final int DEFAULT_QUEUE_WARNING = 5_000;

  public OutboxResilienceProperties {
    if (backoff == null) backoff = DEFAULT_BACKOFF;
    if (inMemoryMaxSize <= 0) inMemoryMaxSize = DEFAULT_MAX_SIZE;
    if (dueQueueWarningThreshold <= 0) dueQueueWarningThreshold =
      DEFAULT_QUEUE_WARNING;
  }
}
