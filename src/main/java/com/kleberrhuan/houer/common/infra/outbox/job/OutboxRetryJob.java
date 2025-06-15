/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox.job;

import com.kleberrhuan.houer.common.application.service.notification.OutboxProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRetryJob {

  private static final String LOCK = "OutboxRetryJob";
  private final OutboxProcessingService processor;
  private static final int BATCH = 50;

  @Scheduled(fixedDelayString = "${app.outbox.retry-interval:30s}")
  @SchedulerLock(name = LOCK, lockAtMostFor = "PT25S")
  public void run() {
    int processed = processor.processBatch(BATCH);
    if (processed > 0) log.info("Processed {} outbox messages", processed);
  }
}
