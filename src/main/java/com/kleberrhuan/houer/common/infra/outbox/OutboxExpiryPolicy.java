/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.github.benmanes.caffeine.cache.Expiry;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class OutboxExpiryPolicy implements Expiry<UUID, OutboxMessage> {

  @Override
  public long expireAfterCreate(
    @NotNull UUID key,
    OutboxMessage msg,
    long currentTimeNanos
  ) {
    return toNanosUntil(msg.getNextAttemptAt(), currentTimeNanos);
  }

  @Override
  public long expireAfterUpdate(
    @NotNull UUID key,
    OutboxMessage msg,
    long currentTimeNanos,
    long currentDurationNanos
  ) {
    return toNanosUntil(msg.getNextAttemptAt(), currentTimeNanos);
  }

  @Override
  public long expireAfterRead(
    @NotNull UUID key,
    @NotNull OutboxMessage msg,
    long currentTimeNanos,
    long currentDurationNanos
  ) {
    return currentDurationNanos;
  }

  private long toNanosUntil(Instant when, long currentTimeNanos) {
    long nowMillis = System.currentTimeMillis();
    long targetMillis = when.toEpochMilli();
    long delayMillis = Math.max(0, targetMillis - nowMillis);
    return TimeUnit.MILLISECONDS.toNanos(delayMillis);
  }
}
