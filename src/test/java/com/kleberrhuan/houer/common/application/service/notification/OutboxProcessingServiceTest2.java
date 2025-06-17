/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxProcessingService")
class OutboxProcessingServiceTest2 {

  @Mock
  OutboxStore store;

  @Mock
  OutboxProcessor<OutboxMessage> processor;

  @Mock
  MeterRegistry meter;

  @Mock
  Counter counter;

  @Mock
  Timer timer;

  private OutboxProcessingService service;

  @BeforeEach
  void setup() {
    service = new OutboxProcessingService(store, processor);
  }

  private OutboxMessage dummyMsg() {
    OutboxMessage msg = new OutboxMessage();
    msg.setId(UUID.randomUUID());
    return msg;
  }

  @Test
  @DisplayName("retorna 0 quando não há mensagens")
  void noMessages() {
    when(store.pollNextDue(anyInt())).thenReturn(List.of());
    int processed = service.processBatch(10);
    assertThat(processed).isZero();
    verify(processor, never()).process(any());
  }

  @Nested
  class OneMessage {

    @Test
    @DisplayName("processamento OK deve deletar mensagem")
    void success() {
      OutboxMessage msg = dummyMsg();
      when(processor.process(msg)).thenReturn(true);

      int processed = service.processBatch(5);
      assertThat(processed).isEqualTo(1);
      verify(store).delete(msg.getId());
      verify(counter).increment();
    }

    @Test
    @DisplayName("processamento retorna false deve requeuear")
    void requeue() {
      OutboxMessage msg = dummyMsg();
      when(processor.process(msg)).thenReturn(false);

      service.processBatch(5);

      verify(processor).nack(msg, store);
      verify(store, never()).delete(any());
      verify(counter).increment();
    }

    @Test
    @DisplayName("exceção durante processamento deve requeuear e contar falha")
    void failure() {
      OutboxMessage msg = dummyMsg();
      when(processor.process(msg)).thenThrow(new RuntimeException("boom"));

      service.processBatch(5);

      verify(processor).nack(msg, store);
      verify(counter).increment();
    }
  }
}
