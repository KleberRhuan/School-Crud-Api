/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.EmailNotification;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailOutboxProcessor")
class EmailOutboxProcessorTest {

  @Mock
  private EmailNotification emailNotification;

  @Mock
  private OutboxStore outboxStore;

  @InjectMocks
  private EmailOutboxProcessor processor;

  private OutboxMessage testMessage;

  @BeforeEach
  void setup() {
    testMessage =
      OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test Subject")
        .body("Test Message Body")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now().plus(Duration.ofMinutes(5)))
        .attempts(0)
        .build();
  }

  @Nested
  @DisplayName("process")
  class Process {

    @Test
    @DisplayName("deve processar mensagem com sucesso e retornar true")
    void givenValidMessage_whenProcess_thenReturnTrue() {
      // Arrange
      doNothing().when(emailNotification).send(any(NotificationModel.class));

      // Act
      boolean result = processor.process(testMessage);

      // Assert
      assertThat(result).isTrue();
      verify(emailNotification)
        .send(
          argThat(notification ->
            notification.channel() == Channel.EMAIL &&
            "test@example.com".equals(notification.to()) &&
            "Test Subject".equals(notification.subject()) &&
            "Test Message Body".equals(notification.message())
          )
        );
    }

    @Test
    @DisplayName(
      "deve converter OutboxMessage para NotificationModel corretamente"
    )
    void givenOutboxMessage_whenProcess_thenConvertToCorrectNotificationModel() {
      // Arrange
      OutboxMessage smsMessage = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("+5511999999999")
        .subject("SMS Code")
        .body("Your code: 123456")
        .channel(Channel.SMS)
        .nextAttemptAt(Instant.now())
        .attempts(1)
        .build();

      // Act
      processor.process(smsMessage);

      // Assert
      verify(emailNotification)
        .send(
          argThat(notification ->
            notification.channel() == Channel.SMS &&
            "+5511999999999".equals(notification.to()) &&
            "SMS Code".equals(notification.subject()) &&
            "Your code: 123456".equals(notification.message())
          )
        );
    }

    @Test
    @DisplayName("deve propagar exceção quando emailNotification falha")
    void givenEmailNotificationFails_whenProcess_thenPropagateException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException(
        "Email service failed"
      );
      doThrow(expectedException)
        .when(emailNotification)
        .send(any(NotificationModel.class));

      // Act & Assert
      assertThatThrownBy(() -> processor.process(testMessage))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Email service failed");

      verify(emailNotification).send(any(NotificationModel.class));
    }

    @Test
    @DisplayName("deve processar mensagem com tentativas anteriores")
    void givenMessageWithPreviousAttempts_whenProcess_thenProcessSuccessfully() {
      // Arrange
      OutboxMessage messageWithAttempts = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("retry@example.com")
        .subject("Retry Subject")
        .body("Retry Message")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(3) // Mensagem já foi tentada 3 vezes
        .build();

      // Act
      boolean result = processor.process(messageWithAttempts);

      // Assert
      assertThat(result).isTrue();
      verify(emailNotification)
        .send(
          argThat(notification -> "retry@example.com".equals(notification.to()))
        );
    }

    @Test
    @DisplayName("deve processar mensagem PUSH corretamente")
    void givenPushMessage_whenProcess_thenProcessSuccessfully() {
      // Arrange
      OutboxMessage pushMessage = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("device-token-123")
        .subject("Push Notification")
        .body("You have a new message")
        .channel(Channel.PUSH)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Act
      boolean result = processor.process(pushMessage);

      // Assert
      assertThat(result).isTrue();
      verify(emailNotification)
        .send(
          argThat(notification ->
            notification.channel() == Channel.PUSH &&
            "device-token-123".equals(notification.to())
          )
        );
    }
  }

  @Nested
  @DisplayName("nack")
  class Nack {

    @Test
    @DisplayName("deve incrementar tentativas e salvar mensagem na store")
    void givenFailedMessage_whenNack_thenIncrementAttemptsAndSave() {
      // Arrange
      int originalAttempts = testMessage.getAttempts();
      Instant originalNextAttempt = testMessage.getNextAttemptAt();

      // Act
      processor.nack(testMessage, outboxStore);

      // Assert
      assertThat(testMessage.getAttempts()).isEqualTo(originalAttempts + 1);
      assertThat(testMessage.getNextAttemptAt()).isAfter(originalNextAttempt);
      verify(outboxStore).save(testMessage);
    }

    @Test
    @DisplayName("deve aplicar backoff exponencial no nextAttemptAt")
    void givenMessageWithMultipleAttempts_whenNack_thenApplyExponentialBackoff() {
      // Arrange
      testMessage.setAttempts(2); // 2^3 = 8 minutes após próxima falha
      Instant beforeNack = testMessage.getNextAttemptAt();

      // Act
      processor.nack(testMessage, outboxStore);

      // Assert
      assertThat(testMessage.getAttempts()).isEqualTo(3);
      assertThat(testMessage.getNextAttemptAt()).isAfter(beforeNack);
      // Deve ter adicionado pelo menos alguns minutos (backoff exponencial)
      Duration backoffDuration = Duration.between(
        beforeNack,
        testMessage.getNextAttemptAt()
      );
      assertThat(backoffDuration.toMinutes()).isGreaterThan(1);
    }

    @Test
    @DisplayName("deve salvar mensagem modificada na store")
    void givenMessage_whenNack_thenSaveModifiedMessage() {
      // Arrange
      UUID messageId = testMessage.getId();
      int originalAttempts = testMessage.getAttempts();

      // Act
      processor.nack(testMessage, outboxStore);

      // Assert
      verify(outboxStore)
        .save(
          argThat(savedMessage ->
            savedMessage.getId().equals(messageId) &&
            savedMessage.getAttempts() == originalAttempts + 1 &&
            savedMessage.getNextAttemptAt().isAfter(Instant.now())
          )
        );
    }

    @Test
    @DisplayName("deve lidar com mensagem que já teve muitas tentativas")
    void givenMessageWithManyAttempts_whenNack_thenHandleGracefully() {
      // Arrange
      testMessage.setAttempts(10); // Muitas tentativas
      Instant beforeNack = testMessage.getNextAttemptAt();

      // Act
      processor.nack(testMessage, outboxStore);

      // Assert
      assertThat(testMessage.getAttempts()).isEqualTo(11);
      assertThat(testMessage.getNextAttemptAt()).isAfter(beforeNack);

      // Backoff deve ser limitado (não crescer indefinidamente)
      Duration backoffDuration = Duration.between(
        beforeNack,
        testMessage.getNextAttemptAt()
      );
      assertThat(backoffDuration.toMinutes()).isLessThanOrEqualTo(60); // Máximo 60 minutos

      verify(outboxStore).save(testMessage);
    }

    @Test
    @DisplayName("deve preservar todos os outros campos da mensagem")
    void givenMessage_whenNack_thenPreserveOtherFields() {
      // Arrange
      UUID originalId = testMessage.getId();
      String originalRecipient = testMessage.getRecipient();
      String originalSubject = testMessage.getSubject();
      String originalBody = testMessage.getBody();
      Channel originalChannel = testMessage.getChannel();

      // Act
      processor.nack(testMessage, outboxStore);

      // Assert
      assertThat(testMessage.getId()).isEqualTo(originalId);
      assertThat(testMessage.getRecipient()).isEqualTo(originalRecipient);
      assertThat(testMessage.getSubject()).isEqualTo(originalSubject);
      assertThat(testMessage.getBody()).isEqualTo(originalBody);
      assertThat(testMessage.getChannel()).isEqualTo(originalChannel);
    }
  }

  @Nested
  @DisplayName("integration scenarios")
  class IntegrationScenarios {

    @Test
    @DisplayName("deve processar e fazer nack de mensagem em cenário completo")
    void givenCompleteScenario_whenProcessAndNack_thenHandleBothOperations() {
      // Arrange
      doThrow(new RuntimeException("Temporary failure"))
        .when(emailNotification)
        .send(any(NotificationModel.class));

      // Act - Primeira tentativa de processamento (deve falhar)
      assertThatThrownBy(() -> processor.process(testMessage))
        .isInstanceOf(RuntimeException.class);

      // Act - Nack da mensagem falhada
      processor.nack(testMessage, outboxStore);

      // Assert
      assertThat(testMessage.getAttempts()).isEqualTo(1);
      verify(emailNotification).send(any(NotificationModel.class));
      verify(outboxStore).save(testMessage);
    }

    @Test
    @DisplayName("deve processar diferentes tipos de canal corretamente")
    void givenDifferentChannels_whenProcess_thenProcessAllChannels() {
      // Arrange
      OutboxMessage emailMsg = createMessageForChannel(
        Channel.EMAIL,
        "email@test.com"
      );
      OutboxMessage smsMsg = createMessageForChannel(
        Channel.SMS,
        "+5511999999999"
      );
      OutboxMessage pushMsg = createMessageForChannel(
        Channel.PUSH,
        "device-token"
      );

      // Act
      boolean emailResult = processor.process(emailMsg);
      boolean smsResult = processor.process(smsMsg);
      boolean pushResult = processor.process(pushMsg);

      // Assert
      assertThat(emailResult).isTrue();
      assertThat(smsResult).isTrue();
      assertThat(pushResult).isTrue();
      verify(emailNotification, times(3)).send(any(NotificationModel.class));
    }
  }

  private OutboxMessage createMessageForChannel(
    Channel channel,
    String recipient
  ) {
    return OutboxMessage
      .builder()
      .id(UUID.randomUUID())
      .recipient(recipient)
      .subject("Test " + channel.name())
      .body("Test message for " + channel.name())
      .channel(channel)
      .nextAttemptAt(Instant.now())
      .attempts(0)
      .build();
  }
}
