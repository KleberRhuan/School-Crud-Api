/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OutboxMessage")
class OutboxMessageTest {

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("deve criar OutboxMessage com todos os campos obrigatórios")
    void givenValidParameters_whenCreate_thenCreateOutboxMessage() {
      // Arrange
      UUID id = UUID.randomUUID();
      String recipient = "test@example.com";
      String subject = "Test Subject";
      String body = "Test Body";
      Channel channel = Channel.EMAIL;
      Instant nextAttemptAt = Instant.now().plus(Duration.ofMinutes(5));

      // Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(id)
        .recipient(recipient)
        .subject(subject)
        .body(body)
        .channel(channel)
        .nextAttemptAt(nextAttemptAt)
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getId()).isEqualTo(id);
      assertThat(message.getRecipient()).isEqualTo(recipient);
      assertThat(message.getSubject()).isEqualTo(subject);
      assertThat(message.getBody()).isEqualTo(body);
      assertThat(message.getChannel()).isEqualTo(channel);
      assertThat(message.getNextAttemptAt()).isEqualTo(nextAttemptAt);
      assertThat(message.getAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("deve criar OutboxMessage para canal EMAIL")
    void givenEmailChannel_whenCreate_thenCreateEmailMessage() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("email@test.com")
        .subject("Email Subject")
        .body("Email Body")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getChannel()).isEqualTo(Channel.EMAIL);
      assertThat(message.getRecipient()).contains("@");
    }

    @Test
    @DisplayName("deve criar OutboxMessage para canal SMS")
    void givenSmsChannel_whenCreate_thenCreateSmsMessage() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("+5511999999999")
        .subject("SMS Code")
        .body("Your verification code is: 123456")
        .channel(Channel.SMS)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getChannel()).isEqualTo(Channel.SMS);
      assertThat(message.getRecipient()).startsWith("+");
    }

    @Test
    @DisplayName("deve criar OutboxMessage para canal PUSH")
    void givenPushChannel_whenCreate_thenCreatePushMessage() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("device-token-123")
        .subject("Push Notification")
        .body("You have a new message")
        .channel(Channel.PUSH)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getChannel()).isEqualTo(Channel.PUSH);
      assertThat(message.getRecipient()).isEqualTo("device-token-123");
    }

    @Test
    @DisplayName("deve criar OutboxMessage com número de tentativas específico")
    void givenSpecificAttempts_whenCreate_thenSetCorrectAttempts() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Retry Message")
        .body("This is a retry")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(3)
        .build();

      // Assert
      assertThat(message.getAttempts()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("toNotification")
  class ToNotification {

    @Test
    @DisplayName(
      "deve converter OutboxMessage para NotificationModel corretamente"
    )
    void givenOutboxMessage_whenToNotification_thenReturnCorrectNotificationModel() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test Subject")
        .body("Test Body")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(1)
        .build();

      // Act
      NotificationModel notification = message.toNotification();

      // Assert
      assertThat(notification.to()).isEqualTo("test@example.com");
      assertThat(notification.subject()).isEqualTo("Test Subject");
      assertThat(notification.message()).isEqualTo("Test Body");
      assertThat(notification.channel()).isEqualTo(Channel.EMAIL);
    }

    @Test
    @DisplayName("deve converter mensagem SMS para NotificationModel")
    void givenSmsMessage_whenToNotification_thenReturnSmsNotification() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("+5511987654321")
        .subject("Verification Code")
        .body("Your code: 456789")
        .channel(Channel.SMS)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Act
      NotificationModel notification = message.toNotification();

      // Assert
      assertThat(notification.to()).isEqualTo("+5511987654321");
      assertThat(notification.subject()).isEqualTo("Verification Code");
      assertThat(notification.message()).isEqualTo("Your code: 456789");
      assertThat(notification.channel()).isEqualTo(Channel.SMS);
    }

    @Test
    @DisplayName("deve converter mensagem PUSH para NotificationModel")
    void givenPushMessage_whenToNotification_thenReturnPushNotification() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("firebase-token-xyz")
        .subject("New Update")
        .body("Your app has been updated")
        .channel(Channel.PUSH)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Act
      NotificationModel notification = message.toNotification();

      // Assert
      assertThat(notification.to()).isEqualTo("firebase-token-xyz");
      assertThat(notification.subject()).isEqualTo("New Update");
      assertThat(notification.message()).isEqualTo("Your app has been updated");
      assertThat(notification.channel()).isEqualTo(Channel.PUSH);
    }

    @Test
    @DisplayName("deve preservar todos os campos na conversão")
    void givenComplexMessage_whenToNotification_thenPreserveAllFields() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("complex@example.com")
        .subject("Complex Subject with Unicode: áéíóú")
        .body("Complex Body with\nmultiple lines\nand special chars: !@#$%")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(5)
        .build();

      // Act
      NotificationModel notification = message.toNotification();

      // Assert
      assertThat(notification.to()).isEqualTo("complex@example.com");
      assertThat(notification.subject())
        .isEqualTo("Complex Subject with Unicode: áéíóú");
      assertThat(notification.message())
        .isEqualTo(
          "Complex Body with\nmultiple lines\nand special chars: !@#$%"
        );
      assertThat(notification.channel()).isEqualTo(Channel.EMAIL);
    }
  }

  @Nested
  @DisplayName("modification methods")
  class ModificationMethods {

    @Test
    @DisplayName("deve modificar número de tentativas corretamente")
    void givenMessage_whenSetAttempts_thenUpdateAttempts() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test")
        .body("Test")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Act
      message.setAttempts(3);

      // Assert
      assertThat(message.getAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("deve modificar nextAttemptAt corretamente")
    void givenMessage_whenSetNextAttemptAt_thenUpdateNextAttemptAt() {
      // Arrange
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test")
        .body("Test")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      Instant newNextAttempt = Instant.now().plus(Duration.ofHours(1));

      // Act
      message.setNextAttemptAt(newNextAttempt);

      // Assert
      assertThat(message.getNextAttemptAt()).isEqualTo(newNextAttempt);
    }
  }

  @Nested
  @DisplayName("edge cases")
  class EdgeCases {

    @Test
    @DisplayName("deve lidar com subject e body vazios")
    void givenEmptySubjectAndBody_whenCreate_thenHandleGracefully() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("")
        .body("")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getSubject()).isEmpty();
      assertThat(message.getBody()).isEmpty();

      NotificationModel notification = message.toNotification();
      assertThat(notification.subject()).isEmpty();
      assertThat(notification.message()).isEmpty();
    }

    @Test
    @DisplayName("deve lidar com valores de tentativas altos")
    void givenHighAttemptCount_whenCreate_thenHandleCorrectly() {
      // Arrange & Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("High Attempts")
        .body("This message had many attempts")
        .channel(Channel.EMAIL)
        .nextAttemptAt(Instant.now())
        .attempts(999)
        .build();

      // Assert
      assertThat(message.getAttempts()).isEqualTo(999);
    }

    @Test
    @DisplayName("deve lidar com nextAttemptAt no passado")
    void givenPastNextAttemptAt_whenCreate_thenAcceptValue() {
      // Arrange
      Instant pastTime = Instant.now().minus(Duration.ofHours(1));

      // Act
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Past Attempt")
        .body("This attempt time is in the past")
        .channel(Channel.EMAIL)
        .nextAttemptAt(pastTime)
        .attempts(0)
        .build();

      // Assert
      assertThat(message.getNextAttemptAt()).isEqualTo(pastTime);
      assertThat(message.getNextAttemptAt()).isBefore(Instant.now());
    }
  }
}
