/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Slf4j
@RequiredArgsConstructor
public class InMemoryOutboxStore implements OutboxStore {

  private static final String METRIC_QUEUE_SIZE = "outbox.due.queue.size";

  private final OutboxResilienceProperties cfg;
  private final MeterRegistry metrics;
  private final BlockingQueue<OutboxMessage> dueMessages =
    new LinkedBlockingQueue<>();
  private final OutboxExpiryPolicy expiryPolicy = new OutboxExpiryPolicy();
  private final Cache<UUID, OutboxMessage> messageCache = Caffeine
    .newBuilder()
    .maximumSize(cfg.inMemoryMaxSize())
    .expireAfter(expiryPolicy)
    .removalListener(this::onRemoval)
    .build();

  private long expiryDelayNanos(OutboxMessage v) {
    long delayMs =
      v.getNextAttemptAt().toEpochMilli() - Instant.now().toEpochMilli();
    return TimeUnit.MILLISECONDS.toNanos(Math.max(0, delayMs));
  }

  private void onRemoval(UUID k, OutboxMessage v, RemovalCause cause) {
    if (v != null && cause == RemovalCause.EXPIRED) {
      dueMessages.offer(v);
    }
  }

  @Override
  public void save(OutboxMessage msg) {
    messageCache.put(msg.getId(), msg);
  }

  @Override
  public Optional<OutboxMessage> pollNextDue() {
    metrics.gauge(METRIC_QUEUE_SIZE, dueMessages, BlockingQueue::size);
    if (dueMessages.size() > cfg.dueQueueWarningThreshold()) {
      log.warn(
        "Due queue size {} exceeded threshold {}",
        dueMessages.size(),
        cfg.dueQueueWarningThreshold()
      );
    }
    return Optional.ofNullable(dueMessages.poll());
  }

  @Override
  public void delete(UUID id) {
    messageCache.invalidate(id);
  }

  @Override
  public StoreHealth health() {
    return StoreHealth.UP;
  }
}
