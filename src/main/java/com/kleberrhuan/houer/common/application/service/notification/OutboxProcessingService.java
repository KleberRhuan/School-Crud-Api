/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessingService {

  private static final String TAG_KEY = "result";
  private static final String VAL_OK = "success";
  private static final String VAL_FAIL = "failure";
  private static final String VAL_NACK = "requeue";

  private final OutboxStore store;
  private final OutboxProcessor<OutboxMessage> processor;
  private final MeterRegistry meter;

  @Transactional
  public int processBatch(int batchSize) {
    String storeName = store.getClass().getSimpleName();
    var pendingMessages = store.pollNextDue(batchSize);
    if (pendingMessages.isEmpty()) return 0;

    pendingMessages.forEach(msg -> processOne(msg, storeName));
    return pendingMessages.size();
  }

  /* ---------------- helpers ---------------- */

  private void processOne(OutboxMessage msg, String storeName) {
    Timer.Sample t = Timer.start(meter);
    try {
      boolean ok = processor.process(msg);
      if (ok) {
        store.delete(msg.getId());
        count(storeName, VAL_OK);
      } else {
        requeue(msg, storeName);
      }
    } catch (Exception ex) {
      log.warn("Error processing msg {}", msg.getId(), ex);
      requeue(msg, storeName);
      count(storeName, VAL_FAIL);
    } finally {
      t.stop(meter.timer("outbox.latency", "store", storeName));
    }
  }

  private void requeue(OutboxMessage msg, String storeName) {
    processor.nack(msg, store);
    count(storeName, VAL_NACK);
  }

  private void count(String storeName, String tag) {
    meter
      .counter("outbox.process", "store", storeName, TAG_KEY, tag)
      .increment();
  }
}
