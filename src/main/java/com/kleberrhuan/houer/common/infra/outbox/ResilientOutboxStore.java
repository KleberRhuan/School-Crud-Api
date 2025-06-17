/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.infra.exception.OutboxNotFoundException;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class ResilientOutboxStore implements OutboxStore {

  private final ObjectProvider<OutboxStore> providers;
  private final CircuitBreaker breaker;
  private final Clock clock;
  private final OutboxResilienceProperties props;

  private List<OutboxStore> stores;
  private final ConcurrentMap<String, Instant> downUntil =
    new ConcurrentHashMap<>();

  @PostConstruct
  void init() {
    stores =
      providers
        .stream()
        .filter(s -> s != this)
        .sorted(AnnotationAwareOrderComparator.INSTANCE)
        .toList();
    stores.forEach(s -> downUntil.put(s.getClass().getSimpleName(), Instant.MIN)
    );
  }

  @Override
  public void save(OutboxMessage m) {
    execute(
      "save",
      s -> {
        s.save(m);
        return null;
      }
    );
  }

  @Override
  public void delete(UUID id) {
    execute(
      "delete",
      s -> {
        s.delete(id);
        return null;
      }
    );
  }

  @Override
  public Optional<OutboxMessage> pollNextDue() {
    return execute("poll", OutboxStore::pollNextDue);
  }

  @Override
  public List<OutboxMessage> pollNextDue(int n) {
    return execute("pollBatch", s -> s.pollNextDue(n));
  }

  @Override
  public StoreHealth health() {
    return stores
        .stream()
        .anyMatch(s -> s.isUp() && s.health() == StoreHealth.UP)
      ? StoreHealth.UP
      : StoreHealth.DOWN;
  }

  /* ---------- helpers ---------------------------------- */

  @Observed(
    name = "outbox.operations",
    lowCardinalityKeyValues = { "op", "#op", "store", "#storeName" }
  )
  private <T> T execute(String op, Function<OutboxStore, T> fn) {
    for (OutboxStore store : stores) {
      if (isDown(store)) continue;

      String storeName = store.getClass().getSimpleName();

      try {
        T result = breaker.executeCallable(() -> fn.apply(store));
        log.debug("Outbox {} OK - store={}", op, storeName);
        return result;
      } catch (Exception ex) {
        markDown(store, ex);
        log.warn(
          "Store {} unavailable for {}: {}",
          storeName,
          op,
          ex.getMessage()
        );
      }
    }
    throw new OutboxNotFoundException();
  }

  private boolean isDown(OutboxStore s) {
    return clock
      .instant()
      .isBefore(downUntil.get(s.getClass().getSimpleName()));
  }

  private void markDown(OutboxStore s, Exception ex) {
    String name = s.getClass().getSimpleName();
    log.warn(
      "Store {} failed until {}",
      name,
      clock.instant().plus(props.backoff())
    );
    downUntil.put(name, clock.instant().plus(props.backoff()));
  }
}
