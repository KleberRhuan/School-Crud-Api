/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.repository.OutboxRepository;
import com.kleberrhuan.houer.common.infra.exception.OutboxPersistenceException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(
  name = "outbox.jdbc.enabled",
  havingValue = "true",
  matchIfMissing = true
)
@Order(2)
@RequiredArgsConstructor
public class JpaOutboxStore implements OutboxStore {

  private final OutboxRepository repo;
  private final JdbcTemplate jdbc;

  @Override
  public void save(OutboxMessage msg) {
    try {
      repo.save(msg);
    } catch (DataAccessException ex) {
      throw new OutboxPersistenceException();
    }
  }

  @Transactional
  @Override
  public Optional<OutboxMessage> pollNextDue() {
    return repo.findNextDueAndLock(Instant.now());
  }

  @Override
  public void delete(UUID id) {
    repo.deleteById(id);
  }

  @Override
  public StoreHealth health() {
    try {
      jdbc.queryForObject("SELECT 1", Integer.class);
      return StoreHealth.UP;
    } catch (Exception ex) {
      return StoreHealth.DOWN;
    }
  }
}
