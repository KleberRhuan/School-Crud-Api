/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.repository.OutboxRepository;
import com.kleberrhuan.houer.common.infra.exception.OutboxPersistenceException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JpaOutboxStore implements OutboxStore {

  private final OutboxRepository repo;
  private final JdbcTemplate jdbc;

  @Override
  public void save(OutboxMessage m) {
    try {
      repo.save(m);
    } catch (DataAccessException e) {
      throw new OutboxPersistenceException();
    }
  }

  @Transactional
  @Override
  public Optional<OutboxMessage> pollNextDue() {
    return repo.findNextDueAndLock(Instant.now());
  }

  @Transactional
  @Override
  public List<OutboxMessage> pollNextDue(int batchSize) {
    log.debug("Polling batch of {} outbox messages", batchSize);
    try {
      List<OutboxMessage> messages = repo.findNextDueBatchAndLock(
        Instant.now(),
        batchSize
      );
      log.debug("Found {} outbox messages for processing", messages.size());
      return messages;
    } catch (DataAccessException e) {
      log.error("Error polling outbox messages: {}", e.getMessage());
      throw new OutboxPersistenceException();
    }
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
    } catch (Exception e) {
      return StoreHealth.DOWN;
    }
  }
}
