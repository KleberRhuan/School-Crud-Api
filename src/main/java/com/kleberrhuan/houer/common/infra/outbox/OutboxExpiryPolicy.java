/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.github.benmanes.caffeine.cache.Expiry;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OutboxExpiryPolicy implements Expiry<UUID, OutboxMessage> {

  private long until(OutboxMessage v) {
    long diff =
      v.getNextAttemptAt().toEpochMilli() - System.currentTimeMillis();
    return TimeUnit.MILLISECONDS.toNanos(Math.max(0, diff));
  }

  @Override
  public long expireAfterCreate(UUID k, OutboxMessage v, long ct) {
    return until(v);
  }

  @Override
  public long expireAfterUpdate(UUID k, OutboxMessage v, long ct, long cd) {
    return until(v);
  }

  @Override
  public long expireAfterRead(UUID k, OutboxMessage v, long ct, long cd) {
    return cd;
  }
}
