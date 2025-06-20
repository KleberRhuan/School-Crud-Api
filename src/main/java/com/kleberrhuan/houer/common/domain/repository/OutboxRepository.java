/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.repository;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
        SELECT m
          FROM OutboxMessage m
         WHERE m.nextAttemptAt <= :now
      ORDER BY m.nextAttemptAt ASC
      """
  )
  Optional<OutboxMessage> findNextDueAndLock(@Param("now") Instant now);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
        SELECT m
          FROM OutboxMessage m
         WHERE m.nextAttemptAt <= :now
      ORDER BY m.nextAttemptAt ASC
      LIMIT :batchSize
      """
  )
  List<OutboxMessage> findNextDueBatchAndLock(
    @Param("now") Instant now,
    @Param("batchSize") int batchSize
  );
}
