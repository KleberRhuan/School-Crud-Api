/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.infra.exception.OutboxNotFoundException;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class ResilientOutboxStore implements OutboxStore {

  private final ObjectProvider<OutboxStore> providers;
  private final MeterRegistry metrics;
  private final OutboxResilienceProperties cfg;
  private List<OutboxStore> stores;
  private final ConcurrentMap<String, Long> downUntil =
    new ConcurrentHashMap<>();

  @PostConstruct
  void init() {
    stores =
      providers
        .stream()
        .filter(s -> s != this)
        .sorted(AnnotationAwareOrderComparator.INSTANCE)
        .toList();
    stores.forEach(s -> downUntil.put(s.getClass().getSimpleName(), 0L));
  }

  @Override
  public void save(OutboxMessage m) {
    run(
      s -> {
        s.save(m);
        return null;
      },
      "save"
    );
  }

  @Override
  public void delete(UUID id) {
    run(
      s -> {
        s.delete(id);
        return null;
      },
      "delete"
    );
  }

  @Override
  public Optional<OutboxMessage> pollNextDue() {
    return run(OutboxStore::pollNextDue, "poll");
  }

  @Override
  public StoreHealth health() {
    return stores
        .stream()
        .anyMatch(s -> isDown(s) && s.health() == StoreHealth.UP)
      ? StoreHealth.UP
      : StoreHealth.DOWN;
  }

  @SneakyThrows
  private <T> T run(Function<OutboxStore, T> op, String opName) {
    for (OutboxStore s : stores) if (isDown(s)) {
      String name = s.getClass().getSimpleName();
      Timer.Sample sample = Timer.start(metrics);
      try {
        T out = op.apply(s);
        metrics
          .counter("outbox." + opName + ".success", "store", name)
          .increment();
        return out;
      } catch (Exception ex) {
        handleFailure(name, ex);
      } finally {
        sample.stop(
          metrics.timer("outbox." + opName + ".latency", "store", name)
        );
      }
    }
    throw new OutboxNotFoundException();
  }

  /* ------------- utils ------------- */
  private boolean isDown(OutboxStore s) {
    return (
      System.currentTimeMillis() >= downUntil.get(s.getClass().getSimpleName())
    );
  }

  private void handleFailure(String name, Exception ex) {
    log.warn("OutboxStore {} falhou: {}", name, ex.getMessage());
    downUntil.put(name, System.currentTimeMillis() + cfg.backoff().toMillis());
    metrics.counter("outbox.store.failure", "store", name).increment();
  }
}
