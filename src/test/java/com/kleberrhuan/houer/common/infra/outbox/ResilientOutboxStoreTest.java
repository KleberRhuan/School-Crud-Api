/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.infra.exception.OutboxNotFoundException;
import com.kleberrhuan.houer.common.infra.properties.OutboxResilienceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ResilientOutboxStore")
class ResilientOutboxStoreTest {

  @Mock
  private ObjectProvider<OutboxStore> providers;

  @Mock
  private CircuitBreaker circuitBreaker;

  @Mock
  private OutboxResilienceProperties properties;

  @Mock
  private OutboxStore primaryStore;

  @Mock
  private OutboxStore fallbackStore;

  @Mock
  private Clock testClock;

  private ResilientOutboxStore resilientStore;

  @BeforeEach
  void setup() throws Exception {
    // Use a very short backoff for tests
    when(properties.backoff()).thenReturn(Duration.ofMillis(1));
    when(providers.stream())
      .thenReturn(Arrays.asList(primaryStore, fallbackStore).stream());

    // Configure circuit breaker to execute callables directly
    when(circuitBreaker.executeCallable(any()))
      .thenAnswer(invocation -> {
        var callable = invocation.getArgument(
          0,
          java.util.concurrent.Callable.class
        );
        try {
          return callable.call();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

    // Configure stores to be UP by default
    when(primaryStore.isUp()).thenReturn(true);
    when(fallbackStore.isUp()).thenReturn(true);
    when(primaryStore.health()).thenReturn(OutboxStore.StoreHealth.UP);
    when(fallbackStore.health()).thenReturn(OutboxStore.StoreHealth.UP);

    // Configure clock to return fixed time
    when(testClock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));

    resilientStore =
      new ResilientOutboxStore(
        providers,
        circuitBreaker,
        testClock,
        properties
      );
    resilientStore.init();
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("deve salvar usando primary store quando disponível")
    void givenPrimaryStoreAvailable_whenSave_thenUsePrimaryStore() {
      OutboxMessage message = createTestMessage();
      resilientStore.save(message);
      verify(primaryStore).save(message);
      verify(fallbackStore, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar exceção quando todos falham")
    void givenAllStoresFail_whenSave_thenThrowException() {
      OutboxMessage message = createTestMessage();
      doThrow(new RuntimeException("Primary failed"))
        .when(primaryStore)
        .save(any());
      doThrow(new RuntimeException("Fallback failed"))
        .when(fallbackStore)
        .save(any());

      assertThatThrownBy(() -> resilientStore.save(message))
        .isInstanceOf(OutboxNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("deve deletar usando primary store")
    void givenPrimaryStoreAvailable_whenDelete_thenUsePrimaryStore() {
      UUID messageId = UUID.randomUUID();
      resilientStore.delete(messageId);
      verify(primaryStore).delete(messageId);
    }

    @Test
    @DisplayName("deve lançar exceção quando todos falham")
    void givenAllStoresFail_whenDelete_thenThrowException() {
      UUID messageId = UUID.randomUUID();
      doThrow(new RuntimeException("Primary failed"))
        .when(primaryStore)
        .delete(any());
      doThrow(new RuntimeException("Fallback failed"))
        .when(fallbackStore)
        .delete(any());

      assertThatThrownBy(() -> resilientStore.delete(messageId))
        .isInstanceOf(OutboxNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("pollNextDue")
  class PollNextDue {

    @Test
    @DisplayName("deve fazer poll usando primary store")
    void givenPrimaryStoreAvailable_whenPollNextDue_thenUsePrimaryStore() {
      OutboxMessage expectedMessage = createTestMessage();
      when(primaryStore.pollNextDue()).thenReturn(Optional.of(expectedMessage));

      Optional<OutboxMessage> result = resilientStore.pollNextDue();

      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(expectedMessage);
      verify(primaryStore).pollNextDue();
    }

    @Test
    @DisplayName("deve lançar exceção quando todos falham")
    void givenAllStoresFail_whenPollNextDue_thenThrowException() {
      doThrow(new RuntimeException("Primary failed"))
        .when(primaryStore)
        .pollNextDue();
      doThrow(new RuntimeException("Fallback failed"))
        .when(fallbackStore)
        .pollNextDue();

      assertThatThrownBy(() -> resilientStore.pollNextDue())
        .isInstanceOf(OutboxNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("health")
  class Health {

    @Test
    @DisplayName("deve retornar UP quando pelo menos um store está UP")
    void givenAtLeastOneStoreUp_whenHealth_thenReturnUp() {
      when(primaryStore.health()).thenReturn(OutboxStore.StoreHealth.DOWN);
      when(fallbackStore.health()).thenReturn(OutboxStore.StoreHealth.UP);

      OutboxStore.StoreHealth health = resilientStore.health();

      assertThat(health).isEqualTo(OutboxStore.StoreHealth.UP);
    }

    @Test
    @DisplayName("deve retornar DOWN quando todos estão DOWN")
    void givenAllStoresDown_whenHealth_thenReturnDown() {
      when(primaryStore.health()).thenReturn(OutboxStore.StoreHealth.DOWN);
      when(fallbackStore.health()).thenReturn(OutboxStore.StoreHealth.DOWN);

      OutboxStore.StoreHealth health = resilientStore.health();

      assertThat(health).isEqualTo(OutboxStore.StoreHealth.DOWN);
    }
  }

  private OutboxMessage createTestMessage() {
    return OutboxMessage
      .builder()
      .id(UUID.randomUUID())
      .recipient("test@example.com")
      .subject("Test Subject")
      .body("Test Body")
      .channel(Channel.EMAIL)
      .nextAttemptAt(Instant.now().plus(Duration.ofMinutes(5)))
      .attempts(0)
      .build();
  }
}
