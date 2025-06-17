/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessingService {

  private static final String VAL_OK = "success";
  private static final String VAL_FAIL = "failure";
  private static final String VAL_NACK = "requeue";

  private final OutboxStore store;
  private final OutboxProcessor<OutboxMessage> processor;

  @Transactional
  public int processBatch(int batchSize) {
    String storeName = store.getClass().getSimpleName();
    var pendingMessages = store.pollNextDue(batchSize);
    if (pendingMessages.isEmpty()) return 0;

    pendingMessages.forEach(msg -> processOne(msg, storeName));
    return pendingMessages.size();
  }

  /* ---------------- helpers ---------------- */

  @Observed(
    name = "outbox.process",
    lowCardinalityKeyValues = { "store", "#storeName" }
  )
  private void processOne(OutboxMessage msg, String storeName) {
    try {
      boolean ok = processor.process(msg);
      if (ok) {
        store.delete(msg.getId());
        log.info("Outbox OK - msg={} store={}", msg.getId(), storeName);
      } else {
        requeue(msg, storeName);
      }
    } catch (Exception ex) {
      log.warn("Error processing msg {}", msg.getId(), ex);
      requeue(msg, storeName);
      log.error("Error processing msg {}: {}", msg.getId(), ex.getMessage());
    }
  }

  private void requeue(OutboxMessage msg, String storeName) {
    processor.nack(msg, store);
    log.warn("Outbox requeue - msg={} store={}", msg.getId(), storeName);
  }
}
