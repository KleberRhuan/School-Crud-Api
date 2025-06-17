/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Slf4j
public class InMemoryOutboxStore implements OutboxStore {

  private static final String METRIC_QUEUE_SIZE = "outbox.due.queue.size";
  private final OutboxResilienceProperties cfg;
  private final MeterRegistry metrics;
  private final BlockingQueue<OutboxMessage> dueMessages =
    new LinkedBlockingQueue<>();
  private final Set<UUID> dueMessageIds = ConcurrentHashMap.newKeySet();
  private final Cache<UUID, OutboxMessage> messageCache;

  public InMemoryOutboxStore(
    OutboxResilienceProperties cfg,
    MeterRegistry metrics
  ) {
    this.cfg = cfg;
    this.metrics = metrics;
    OutboxExpiryPolicy expiryPolicy = new OutboxExpiryPolicy();
    this.messageCache =
      Caffeine
        .newBuilder()
        .maximumSize(cfg.inMemoryMaxSize())
        .expireAfter(expiryPolicy)
        .removalListener(this::onRemoval)
        .build();
  }

  @PostConstruct
  void init() {
    metrics.gauge(METRIC_QUEUE_SIZE, this, s -> s.dueMessages.size());
  }

  private void onRemoval(UUID k, OutboxMessage v, RemovalCause cause) {
    if (
      v != null &&
      (cause == RemovalCause.EXPIRED || cause == RemovalCause.EXPLICIT) &&
      dueMessageIds.add(v.getId())
    ) {
      dueMessages.offer(v);
    }
  }

  @Override
  public void save(OutboxMessage msg) {
    if (msg.getId() == null) {
      msg.setId(UUID.randomUUID());
    }
    messageCache.put(msg.getId(), msg);
  }

  @Override
  public Optional<OutboxMessage> pollNextDue() {
    OutboxMessage message = dueMessages.poll();
    if (message != null) {
      dueMessageIds.remove(message.getId());
      return Optional.of(message);
    }
    return Optional.empty();
  }

  @Override
  public void delete(UUID id) {
    OutboxMessage v = messageCache.getIfPresent(id);
    messageCache.invalidate(id);
    if (v != null && dueMessageIds.add(id)) {
      dueMessages.offer(v);
    }
  }

  @Override
  public StoreHealth health() {
    return StoreHealth.UP;
  }
}
