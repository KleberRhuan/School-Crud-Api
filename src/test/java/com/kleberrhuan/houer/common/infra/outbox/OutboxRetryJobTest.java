/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.service.notification.OutboxProcessingService;
import com.kleberrhuan.houer.common.infra.outbox.job.OutboxRetryJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutboxRetryJobTest {

  @Test
  @DisplayName("run deve chamar processBatch e logar quando >0")
  void shouldInvokeProcessBatch() {
    OutboxProcessingService proc = mock(OutboxProcessingService.class);
    when(proc.processBatch(anyInt())).thenReturn(3);

    OutboxRetryJob job = new OutboxRetryJob(proc);
    job.run();

    verify(proc).processBatch(50);
  }
}
