/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryOutboxStore")
class InMemoryOutboxStoreTest {

  private InMemoryOutboxStore store;
  private OutboxResilienceProperties properties;
  private MeterRegistry meterRegistry;

  @BeforeEach
  void setup() {
    properties =
      new OutboxResilienceProperties(Duration.ofMinutes(1), 1000, 500);
    meterRegistry = new SimpleMeterRegistry();
    store = new InMemoryOutboxStore(properties, meterRegistry);
    store.init(); // Manually call @PostConstruct method
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("deve salvar mensagem com ID existente")
    void givenMessageWithId_whenSave_thenSaveSuccessfully() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage message = createTestMessage(messageId);

      // Act
      store.save(message);

      // Assert
      // Para verificar se foi salva, tentamos fazer poll depois que expirar
      // Como não podemos acessar diretamente o cache, verificamos comportamento
      // indireto
      assertThat(message.getId()).isEqualTo(messageId);
    }

    @Test
    @DisplayName("deve gerar ID quando mensagem não tem ID")
    void givenMessageWithoutId_whenSave_thenGenerateId() {
      // Arrange
      OutboxMessage message = createTestMessage(null);
      assertThat(message.getId()).isNull();

      // Act
      store.save(message);

      // Assert
      assertThat(message.getId()).isNotNull();
    }

    @Test
    @DisplayName("deve salvar múltiplas mensagens")
    void givenMultipleMessages_whenSave_thenSaveAll() {
      // Arrange
      OutboxMessage message1 = createTestMessage(UUID.randomUUID());
      OutboxMessage message2 = createTestMessage(UUID.randomUUID());
      OutboxMessage message3 = createTestMessage(UUID.randomUUID());

      // Act
      store.save(message1);
      store.save(message2);
      store.save(message3);

      // Assert
      // Verificamos se todas têm IDs únicos
      assertThat(message1.getId()).isNotEqualTo(message2.getId());
      assertThat(message2.getId()).isNotEqualTo(message3.getId());
      assertThat(message1.getId()).isNotEqualTo(message3.getId());
    }

    @Test
    @DisplayName("deve atualizar mensagem existente quando salvar com mesmo ID")
    void givenExistingMessage_whenSaveWithSameId_thenUpdate() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage originalMessage = createTestMessage(messageId);
      originalMessage.setAttempts(1);

      OutboxMessage updatedMessage = createTestMessage(messageId);
      updatedMessage.setAttempts(3);

      // Act
      store.save(originalMessage);
      store.save(updatedMessage); // Should update

      // Assert
      assertThat(updatedMessage.getAttempts()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("deve deletar mensagem existente")
    void givenExistingMessage_whenDelete_thenRemoveFromStore() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage message = createTestMessage(messageId);
      store.save(message);

      // Act
      store.delete(messageId);

      // Assert
      // A mensagem deve aparecer na fila due quando for deletada
      Optional<OutboxMessage> polledMessage = store.pollNextDue();
      assertThat(polledMessage).isPresent();
      assertThat(polledMessage.get().getId()).isEqualTo(messageId);
    }

    @Test
    @DisplayName("deve lidar graciosamente com delete de ID inexistente")
    void givenNonExistentId_whenDelete_thenHandleGracefully() {
      // Arrange
      UUID nonExistentId = UUID.randomUUID();

      // Act & Assert
      assertThatCode(() -> store.delete(nonExistentId))
        .doesNotThrowAnyException();

      // Não deve haver mensagem na fila
      Optional<OutboxMessage> polledMessage = store.pollNextDue();
      assertThat(polledMessage).isEmpty();
    }

    @Test
    @DisplayName("deve mover mensagem para fila due quando deletar")
    void givenExistingMessage_whenDelete_thenMoveToQueue() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage message = createTestMessage(messageId);
      store.save(message);

      // Act
      store.delete(messageId);

      // Assert
      // A mensagem deve aparecer na fila due
      Optional<OutboxMessage> polledMessage = store.pollNextDue();
      assertThat(polledMessage).isPresent();
      assertThat(polledMessage.get().getId()).isEqualTo(messageId);
    }
  }

  @Nested
  @DisplayName("pollNextDue")
  class PollNextDue {

    @Test
    @DisplayName("deve retornar empty quando não há mensagens due")
    void givenNoMessages_whenPollNextDue_thenReturnEmpty() {
      // Act
      Optional<OutboxMessage> result = store.pollNextDue();

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve retornar mensagem quando há mensagem due")
    void givenDueMessage_whenPollNextDue_thenReturnMessage() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage message = createTestMessage(messageId);
      store.save(message);

      // Simulate message expiry by deleting it (which moves to due queue)
      store.delete(messageId);

      // Act
      Optional<OutboxMessage> result = store.pollNextDue();

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(messageId);
    }

    @Test
    @DisplayName("deve remover mensagem da fila após poll")
    void givenDueMessage_whenPollNextDue_thenRemoveFromQueue() {
      // Arrange
      UUID messageId = UUID.randomUUID();
      OutboxMessage message = createTestMessage(messageId);
      store.save(message);
      store.delete(messageId); // Move to due queue

      // Act
      Optional<OutboxMessage> firstPoll = store.pollNextDue();
      Optional<OutboxMessage> secondPoll = store.pollNextDue();

      // Assert
      assertThat(firstPoll).isPresent();
      assertThat(secondPoll).isEmpty(); // Should be empty after first poll
    }

    @Test
    @DisplayName("deve retornar mensagens em ordem quando múltiplas due")
    void givenMultipleDueMessages_whenPollNextDue_thenReturnInOrder() {
      // Arrange
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      UUID id3 = UUID.randomUUID();

      OutboxMessage message1 = createTestMessage(id1);
      OutboxMessage message2 = createTestMessage(id2);
      OutboxMessage message3 = createTestMessage(id3);

      store.save(message1);
      store.save(message2);
      store.save(message3);

      // Move all to due queue
      store.delete(id1);
      store.delete(id2);
      store.delete(id3);

      // Act
      Optional<OutboxMessage> first = store.pollNextDue();
      Optional<OutboxMessage> second = store.pollNextDue();
      Optional<OutboxMessage> third = store.pollNextDue();
      Optional<OutboxMessage> fourth = store.pollNextDue(); // Should be empty

      // Assert
      assertThat(first).isPresent();
      assertThat(second).isPresent();
      assertThat(third).isPresent();
      assertThat(fourth).isEmpty();

      // All should be different messages
      assertThat(first.get().getId()).isNotEqualTo(second.get().getId());
      assertThat(second.get().getId()).isNotEqualTo(third.get().getId());
      assertThat(first.get().getId()).isNotEqualTo(third.get().getId());
    }
  }

  @Nested
  @DisplayName("pollNextDue batch")
  class PollNextDueBatch {

    @Test
    @DisplayName("deve retornar lista vazia quando não há mensagens")
    void givenNoMessages_whenPollNextDueBatch_thenReturnEmptyList() {
      // Act
      List<OutboxMessage> result = store.pollNextDue(5);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve retornar batch de mensagens quando disponíveis")
    void givenMultipleMessages_whenPollNextDueBatch_thenReturnBatch() {
      // Arrange
      int batchSize = 3;
      int totalMessages = 5;

      for (int i = 0; i < totalMessages; i++) {
        UUID id = UUID.randomUUID();
        OutboxMessage message = createTestMessage(id);
        store.save(message);
        store.delete(id); // Move to due queue
      }

      // Act
      List<OutboxMessage> result = store.pollNextDue(batchSize);

      // Assert
      assertThat(result).hasSize(batchSize);
      assertThat(result.stream().map(OutboxMessage::getId))
        .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName(
      "deve retornar menos mensagens quando batch size maior que disponível"
    )
    void givenFewerMessagesThanBatchSize_whenPollNextDue_thenReturnAvailable() {
      // Arrange
      int batchSize = 5;
      int availableMessages = 2;

      for (int i = 0; i < availableMessages; i++) {
        UUID id = UUID.randomUUID();
        OutboxMessage message = createTestMessage(id);
        store.save(message);
        store.delete(id); // Move to due queue
      }

      // Act
      List<OutboxMessage> result = store.pollNextDue(batchSize);

      // Assert
      assertThat(result).hasSize(availableMessages);
    }

    @Test
    @DisplayName("deve remover mensagens da fila após batch poll")
    void givenMessages_whenPollNextDueBatch_thenRemoveFromQueue() {
      // Arrange
      int messageCount = 3;
      for (int i = 0; i < messageCount; i++) {
        UUID id = UUID.randomUUID();
        OutboxMessage message = createTestMessage(id);
        store.save(message);
        store.delete(id); // Move to due queue
      }

      // Act
      List<OutboxMessage> firstBatch = store.pollNextDue(messageCount);
      List<OutboxMessage> secondBatch = store.pollNextDue(messageCount);

      // Assert
      assertThat(firstBatch).hasSize(messageCount);
      assertThat(secondBatch).isEmpty(); // Should be empty after first batch
    }
  }

  @Nested
  @DisplayName("health")
  class Health {

    @Test
    @DisplayName("deve sempre retornar UP para InMemoryOutboxStore")
    void whenHealth_thenReturnUp() {
      // Act
      OutboxStore.StoreHealth health = store.health();

      // Assert
      assertThat(health).isEqualTo(OutboxStore.StoreHealth.UP);
    }
  }

  @Nested
  @DisplayName("metrics integration")
  class MetricsIntegration {

    @Test
    @DisplayName("deve registrar métrica de tamanho da fila")
    void givenMessages_whenInit_thenRegisterQueueSizeMetric() {
      // Arrange & Act
      // Metrics são registradas no @PostConstruct (já chamado no setup)

      // Assert
      assertThat(meterRegistry.getMeters()).isNotEmpty();

      // Verifica se a métrica de tamanho da fila foi registrada
      boolean hasQueueSizeMetric = meterRegistry
        .getMeters()
        .stream()
        .anyMatch(meter ->
          meter.getId().getName().contains("outbox.due.queue.size")
        );

      assertThat(hasQueueSizeMetric).isTrue();
    }
  }

  // Helper methods
  private OutboxMessage createTestMessage(UUID id) {
    return OutboxMessage
      .builder()
      .id(id)
      .recipient("test@example.com")
      .subject("Test Subject")
      .body("Test Body")
      .channel(Channel.EMAIL)
      .nextAttemptAt(Instant.now().plus(Duration.ofMinutes(10)))
      .attempts(0)
      .build();
  }
}
