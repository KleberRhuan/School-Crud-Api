/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OutboxProcessingService - Unit Tests")
class OutboxProcessingServiceTest {

  @Mock
  private OutboxStore store;

  @Mock
  private OutboxProcessor<OutboxMessage> processor;

  @Mock(strictness = Mock.Strictness.LENIENT)
  private MeterRegistry meterRegistry;

  @Mock(strictness = Mock.Strictness.LENIENT)
  private Timer timer;

  @Mock(strictness = Mock.Strictness.LENIENT)
  private Counter counter;

  @InjectMocks
  private OutboxProcessingService service;

  private OutboxMessage outboxMessage;

  @BeforeEach
  void setUp() {
    outboxMessage =
      OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test Subject")
        .body("Test Body")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

    // Configure Micrometer mocks properly
    MeterRegistry.Config config = mock(
      MeterRegistry.Config.class,
      withSettings()
    );
    Clock clock = mock(Clock.class, withSettings());

    // Configure the clock to return valid long values
    when(clock.monotonicTime()).thenReturn(System.nanoTime());
    when(clock.wallTime()).thenReturn(System.currentTimeMillis());

    when(meterRegistry.config()).thenReturn(config);
    when(config.clock()).thenReturn(clock);
    when(meterRegistry.timer(anyString(), any(String[].class)))
      .thenReturn(timer);
    when(meterRegistry.counter(anyString(), any(String[].class)))
      .thenReturn(counter);
  }

  @Nested
  @DisplayName("processBatch - Success Scenarios")
  class ProcessBatchSuccessScenarios {

    @Test
    @DisplayName("Should return 0 when no pending messages")
    void shouldReturnZeroWhenNoPendingMessages() {
      // Given
      int batchSize = 10;
      when(store.pollNextDue(batchSize)).thenReturn(Collections.emptyList());

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isZero();
      verify(store).pollNextDue(batchSize);
      verifyNoInteractions(processor);
      verifyNoInteractions(meterRegistry);
    }

    @Test
    @DisplayName("Should process message successfully and delete from store")
    void shouldProcessMessageSuccessfullyAndDeleteFromStore() {
      // Given
      int batchSize = 10;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage)).thenReturn(true);

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isEqualTo(1);
      verify(store).pollNextDue(batchSize);
      verify(processor).process(outboxMessage);
      verify(store).delete(outboxMessage.getId());
      verify(counter).increment();
    }

    @Test
    @DisplayName("Should process multiple messages successfully")
    void shouldProcessMultipleMessagesSuccessfully() {
      // Given
      int batchSize = 10;
      OutboxMessage secondMessage = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test2@example.com")
        .subject("Test Subject 2")
        .body("Test Body 2")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      List<OutboxMessage> messages = List.of(outboxMessage, secondMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(any(OutboxMessage.class))).thenReturn(true);

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isEqualTo(2);
      verify(store).pollNextDue(batchSize);
      verify(processor, times(2)).process(any(OutboxMessage.class));
      verify(store, times(2)).delete(any(UUID.class));
      verify(counter, times(2)).increment();
    }
  }

  @Nested
  @DisplayName("processBatch - Failure Scenarios")
  class ProcessBatchFailureScenarios {

    @Test
    @DisplayName("Should requeue message when processing fails")
    void shouldRequeueMessageWhenProcessingFails() {
      // Given
      int batchSize = 10;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage)).thenReturn(false);

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isEqualTo(1);
      verify(store).pollNextDue(batchSize);
      verify(processor).process(outboxMessage);
      verify(processor).nack(outboxMessage, store);
      verify(store, never()).delete(any(UUID.class));
      verify(counter).increment();
    }

    @Test
    @DisplayName(
      "Should handle exception during processing and requeue message"
    )
    void shouldHandleExceptionDuringProcessingAndRequeueMessage() {
      // Given
      int batchSize = 10;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage))
        .thenThrow(new RuntimeException("Processing error"));

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isEqualTo(1);
      verify(store).pollNextDue(batchSize);
      verify(processor).process(outboxMessage);
      verify(processor).nack(outboxMessage, store);
      verify(store, never()).delete(any(UUID.class));
      verify(counter, times(2)).increment(); // One for requeue, one for failure
    }

    @Test
    @DisplayName("Should process remaining messages even when one fails")
    void shouldProcessRemainingMessagesEvenWhenOneFails() {
      // Given
      int batchSize = 10;
      OutboxMessage secondMessage = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test2@example.com")
        .subject("Test Subject 2")
        .body("Test Body 2")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      List<OutboxMessage> messages = List.of(outboxMessage, secondMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage))
        .thenThrow(new RuntimeException("Error"));
      when(processor.process(secondMessage)).thenReturn(true);

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isEqualTo(2);
      verify(processor).process(outboxMessage);
      verify(processor).process(secondMessage);
      verify(processor).nack(outboxMessage, store);
      verify(store).delete(secondMessage.getId());
      verify(store, never()).delete(outboxMessage.getId());
    }
  }

  @Nested
  @DisplayName("Metrics and Monitoring")
  class MetricsAndMonitoring {

    @Test
    @DisplayName("Should register success metrics correctly")
    void shouldRegisterSuccessMetricsCorrectly() {
      // Given
      int batchSize = 1;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage)).thenReturn(true);

      // When
      service.processBatch(batchSize);

      // Then
      verify(meterRegistry)
        .counter(
          eq("outbox.process"),
          eq("store"),
          contains("OutboxStore"),
          eq("result"),
          eq("success")
        );
      verify(meterRegistry)
        .timer(eq("outbox.latency"), eq("store"), contains("OutboxStore"));
    }

    @Test
    @DisplayName("Should register failure metrics correctly")
    void shouldRegisterFailureMetricsCorrectly() {
      // Given
      int batchSize = 1;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage))
        .thenThrow(new RuntimeException("Error"));

      // When
      service.processBatch(batchSize);

      // Then
      verify(meterRegistry)
        .counter(
          eq("outbox.process"),
          eq("store"),
          contains("OutboxStore"),
          eq("result"),
          eq("requeue")
        );
      verify(meterRegistry)
        .counter(
          eq("outbox.process"),
          eq("store"),
          contains("OutboxStore"),
          eq("result"),
          eq("failure")
        );
      verify(meterRegistry)
        .timer(eq("outbox.latency"), eq("store"), contains("OutboxStore"));
    }

    @Test
    @DisplayName("Should register requeue metrics correctly")
    void shouldRegisterRequeueMetricsCorrectly() {
      // Given
      int batchSize = 1;
      List<OutboxMessage> messages = List.of(outboxMessage);
      when(store.pollNextDue(batchSize)).thenReturn(messages);
      when(processor.process(outboxMessage)).thenReturn(false);

      // When
      service.processBatch(batchSize);

      // Then
      verify(meterRegistry)
        .counter(
          eq("outbox.process"),
          eq("store"),
          contains("OutboxStore"),
          eq("result"),
          eq("requeue")
        );
      verify(meterRegistry)
        .timer(eq("outbox.latency"), eq("store"), contains("OutboxStore"));
    }
  }

  @Nested
  @DisplayName("Input Validations")
  class InputValidations {

    @Test
    @DisplayName("Should process correctly with zero batch size")
    void shouldProcessCorrectlyWithZeroBatchSize() {
      // Given
      int batchSize = 0;
      when(store.pollNextDue(batchSize)).thenReturn(Collections.emptyList());

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isZero();
      verify(store).pollNextDue(batchSize);
    }

    @Test
    @DisplayName("Should process correctly with negative batch size")
    void shouldProcessCorrectlyWithNegativeBatchSize() {
      // Given
      int batchSize = -1;
      when(store.pollNextDue(batchSize)).thenReturn(Collections.emptyList());

      // When
      int result = service.processBatch(batchSize);

      // Then
      assertThat(result).isZero();
      verify(store).pollNextDue(batchSize);
    }
  }
}
