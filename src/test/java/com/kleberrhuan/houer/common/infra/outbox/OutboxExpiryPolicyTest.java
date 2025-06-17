/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.outbox;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OutboxExpiryPolicy")
class OutboxExpiryPolicyTest {

  private OutboxExpiryPolicy policy;

  @BeforeEach
  void setup() {
    policy = new OutboxExpiryPolicy();
  }

  @Nested
  @DisplayName("expireAfterCreate")
  class ExpireAfterCreate {

    @Test
    @DisplayName(
      "deve retornar tempo de expiração baseado no nextAttemptAt futuro"
    )
    void givenFutureNextAttemptAt_whenExpireAfterCreate_thenReturnCorrectNanos() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant futureTime = Instant.now().plus(Duration.ofMinutes(10));
      OutboxMessage message = createMessage(futureTime);
      long currentTime = System.currentTimeMillis();

      // Act
      long expireNanos = policy.expireAfterCreate(key, message, currentTime);

      // Assert
      long expectedNanos = TimeUnit.MILLISECONDS.toNanos(
        futureTime.toEpochMilli() - currentTime
      );
      // Allow for small timing differences (up to 1 second)
      assertThat(expireNanos)
        .isCloseTo(expectedNanos, offset(TimeUnit.SECONDS.toNanos(1)));
      assertThat(expireNanos).isGreaterThan(0);
    }

    @Test
    @DisplayName("deve retornar 0 quando nextAttemptAt é no passado")
    void givenPastNextAttemptAt_whenExpireAfterCreate_thenReturnZero() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant pastTime = Instant.now().minus(Duration.ofMinutes(5));
      OutboxMessage message = createMessage(pastTime);
      long currentTime = System.currentTimeMillis();

      // Act
      long expireNanos = policy.expireAfterCreate(key, message, currentTime);

      // Assert
      assertThat(expireNanos).isEqualTo(0);
    }

    @Test
    @DisplayName("deve retornar 0 quando nextAttemptAt é exatamente agora")
    void givenCurrentNextAttemptAt_whenExpireAfterCreate_thenReturnZero() {
      // Arrange
      UUID key = UUID.randomUUID();
      long currentTimeMillis = System.currentTimeMillis();
      Instant currentTime = Instant.ofEpochMilli(currentTimeMillis);
      OutboxMessage message = createMessage(currentTime);

      // Act
      long expireNanos = policy.expireAfterCreate(
        key,
        message,
        currentTimeMillis
      );

      // Assert
      assertThat(expireNanos).isEqualTo(0);
    }

    @Test
    @DisplayName("deve lidar com nextAttemptAt muito no futuro")
    void givenVeryFutureNextAttemptAt_whenExpireAfterCreate_thenReturnLargeValue() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant veryFutureTime = Instant.now().plus(Duration.ofDays(1));
      OutboxMessage message = createMessage(veryFutureTime);
      long currentTime = System.currentTimeMillis();

      // Act
      long expireNanos = policy.expireAfterCreate(key, message, currentTime);

      // Assert
      assertThat(expireNanos).isGreaterThan(TimeUnit.HOURS.toNanos(1));
    }

    @Test
    @DisplayName("deve lidar com nextAttemptAt null graciosamente")
    void givenNullNextAttemptAt_whenCalculateExpiry_thenHandleGracefully() {
      // Arrange
      UUID key = UUID.randomUUID();
      OutboxMessage message = OutboxMessage
        .builder()
        .id(UUID.randomUUID())
        .recipient("test@example.com")
        .subject("Test")
        .body("Test")
        .channel(Channel.EMAIL)
        .nextAttemptAt(null) // Null nextAttemptAt
        .attempts(0)
        .build();
      long currentTime = System.currentTimeMillis();

      // Act
      long expireNanos = policy.expireAfterCreate(key, message, currentTime);

      // Assert
      assertThat(expireNanos).isEqualTo(0); // Should expire immediately
    }
  }

  @Nested
  @DisplayName("expireAfterUpdate")
  class ExpireAfterUpdate {

    @Test
    @DisplayName(
      "deve retornar tempo de expiração baseado no nextAttemptAt atualizado"
    )
    void givenUpdatedMessage_whenExpireAfterUpdate_thenReturnCorrectNanos() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant futureTime = Instant.now().plus(Duration.ofMinutes(15));
      OutboxMessage message = createMessage(futureTime);
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.MINUTES.toNanos(5); // Previous duration

      // Act
      long expireNanos = policy.expireAfterUpdate(
        key,
        message,
        currentTime,
        currentDuration
      );

      // Assert
      long expectedNanos = TimeUnit.MILLISECONDS.toNanos(
        futureTime.toEpochMilli() - currentTime
      );
      assertThat(expireNanos)
        .isCloseTo(expectedNanos, offset(TimeUnit.SECONDS.toNanos(1)));
      assertThat(expireNanos).isGreaterThan(0);
    }

    @Test
    @DisplayName("deve retornar 0 quando nextAttemptAt atualizado é no passado")
    void givenPastUpdatedNextAttemptAt_whenExpireAfterUpdate_thenReturnZero() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant pastTime = Instant.now().minus(Duration.ofMinutes(3));
      OutboxMessage message = createMessage(pastTime);
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.MINUTES.toNanos(10);

      // Act
      long expireNanos = policy.expireAfterUpdate(
        key,
        message,
        currentTime,
        currentDuration
      );

      // Assert
      assertThat(expireNanos).isEqualTo(0);
    }

    @Test
    @DisplayName(
      "deve ignorar currentDuration e recalcular baseado no novo nextAttemptAt"
    )
    void givenNewNextAttemptAt_whenExpireAfterUpdate_thenIgnoreCurrentDuration() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant newFutureTime = Instant.now().plus(Duration.ofMinutes(20));
      OutboxMessage message = createMessage(newFutureTime);
      long currentTime = System.currentTimeMillis();
      long oldDuration = TimeUnit.MINUTES.toNanos(1); // Should be ignored

      // Act
      long expireNanos = policy.expireAfterUpdate(
        key,
        message,
        currentTime,
        oldDuration
      );

      // Assert
      long expectedNanos = TimeUnit.MILLISECONDS.toNanos(
        newFutureTime.toEpochMilli() - currentTime
      );
      assertThat(expireNanos)
        .isCloseTo(expectedNanos, offset(TimeUnit.SECONDS.toNanos(1)));
      assertThat(expireNanos).isNotEqualTo(oldDuration);
    }

    @Test
    @DisplayName("deve lidar com mensagem com tentativas aumentadas")
    void givenMessageWithIncreasedAttempts_whenExpireAfterUpdate_thenCalculateCorrectly() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant nextAttemptTime = Instant.now().plus(Duration.ofMinutes(30));
      OutboxMessage message = createMessage(nextAttemptTime);
      message.setAttempts(5); // Message has multiple attempts
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.MINUTES.toNanos(2);

      // Act
      long expireNanos = policy.expireAfterUpdate(
        key,
        message,
        currentTime,
        currentDuration
      );

      // Assert
      long expectedNanos = TimeUnit.MILLISECONDS.toNanos(
        nextAttemptTime.toEpochMilli() - currentTime
      );
      assertThat(expireNanos)
        .isCloseTo(expectedNanos, offset(TimeUnit.SECONDS.toNanos(1)));
    }
  }

  @Nested
  @DisplayName("expireAfterRead")
  class ExpireAfterRead {

    @Test
    @DisplayName("deve retornar currentDuration sem modificação")
    void givenAnyMessage_whenExpireAfterRead_thenReturnCurrentDuration() {
      // Arrange
      UUID key = UUID.randomUUID();
      OutboxMessage message = createMessage(
        Instant.now().plus(Duration.ofMinutes(5))
      );
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.MINUTES.toNanos(7);

      // Act
      long expireNanos = policy.expireAfterRead(
        key,
        message,
        currentTime,
        currentDuration
      );

      // Assert
      assertThat(expireNanos).isEqualTo(currentDuration);
    }

    @Test
    @DisplayName(
      "deve retornar currentDuration mesmo com nextAttemptAt no passado"
    )
    void givenPastNextAttemptAt_whenExpireAfterRead_thenReturnCurrentDuration() {
      // Arrange
      UUID key = UUID.randomUUID();
      OutboxMessage message = createMessage(
        Instant.now().minus(Duration.ofMinutes(10))
      );
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.HOURS.toNanos(1);

      // Act
      long expireNanos = policy.expireAfterRead(
        key,
        message,
        currentTime,
        currentDuration
      );

      // Assert
      assertThat(expireNanos).isEqualTo(currentDuration);
    }

    @Test
    @DisplayName("deve retornar currentDuration independente do tempo atual")
    void givenDifferentCurrentTime_whenExpireAfterRead_thenReturnSameDuration() {
      // Arrange
      UUID key = UUID.randomUUID();
      OutboxMessage message = createMessage(
        Instant.now().plus(Duration.ofMinutes(5))
      );
      long currentDuration = TimeUnit.MINUTES.toNanos(15);

      // Act
      long expireNanos1 = policy.expireAfterRead(
        key,
        message,
        System.currentTimeMillis(),
        currentDuration
      );
      long expireNanos2 = policy.expireAfterRead(
        key,
        message,
        System.currentTimeMillis() + 10000,
        currentDuration
      );

      // Assert
      assertThat(expireNanos1).isEqualTo(currentDuration);
      assertThat(expireNanos2).isEqualTo(currentDuration);
      assertThat(expireNanos1).isEqualTo(expireNanos2);
    }

    @Test
    @DisplayName(
      "deve preservar currentDuration independente das características da mensagem"
    )
    void givenVariousMessages_whenExpireAfterRead_thenPreserveDuration() {
      // Arrange
      UUID key = UUID.randomUUID();
      long currentTime = System.currentTimeMillis();
      long currentDuration = TimeUnit.MINUTES.toNanos(25);

      OutboxMessage emailMessage = createMessage(
        Instant.now().plus(Duration.ofMinutes(1))
      );
      OutboxMessage smsMessage = createMessageWithChannel(
        Instant.now().plus(Duration.ofHours(1)),
        Channel.SMS
      );
      OutboxMessage pushMessage = createMessageWithChannel(
        Instant.now().minus(Duration.ofMinutes(5)),
        Channel.PUSH
      );

      // Act
      long emailExpire = policy.expireAfterRead(
        key,
        emailMessage,
        currentTime,
        currentDuration
      );
      long smsExpire = policy.expireAfterRead(
        key,
        smsMessage,
        currentTime,
        currentDuration
      );
      long pushExpire = policy.expireAfterRead(
        key,
        pushMessage,
        currentTime,
        currentDuration
      );

      // Assert
      assertThat(emailExpire).isEqualTo(currentDuration);
      assertThat(smsExpire).isEqualTo(currentDuration);
      assertThat(pushExpire).isEqualTo(currentDuration);
    }
  }

  @Nested
  @DisplayName("edge cases")
  class EdgeCases {

    @Test
    @DisplayName("deve lidar com overflow de tempo graciosamente")
    void givenExtremelyFutureTime_whenCalculateExpiry_thenHandleOverflow() {
      // Arrange
      UUID key = UUID.randomUUID();
      Instant extremeFuture = Instant.ofEpochMilli(Long.MAX_VALUE - 1000);
      OutboxMessage message = createMessage(extremeFuture);
      long currentTime = System.currentTimeMillis();

      // Act
      long expireNanos = policy.expireAfterCreate(key, message, currentTime);

      // Assert
      assertThat(expireNanos).isGreaterThan(0);
    }

    @Test
    @DisplayName("deve calcular corretamente com tempos muito próximos")
    void givenVeryCloseTime_whenCalculateExpiry_thenCalculateAccurately() {
      // Arrange
      UUID key = UUID.randomUUID();
      long currentTimeMillis = System.currentTimeMillis();
      Instant nearFuture = Instant.ofEpochMilli(currentTimeMillis + 100); // 100ms no futuro
      OutboxMessage message = createMessage(nearFuture);

      // Act
      long expireNanos = policy.expireAfterCreate(
        key,
        message,
        currentTimeMillis
      );

      // Assert
      long expectedNanos = TimeUnit.MILLISECONDS.toNanos(100);
      assertThat(expireNanos)
        .isCloseTo(expectedNanos, offset(TimeUnit.MILLISECONDS.toNanos(10)));
    }
  }

  // Helper methods
  private OutboxMessage createMessage(Instant nextAttemptAt) {
    return OutboxMessage
      .builder()
      .id(UUID.randomUUID())
      .recipient("test@example.com")
      .subject("Test Subject")
      .body("Test Body")
      .channel(Channel.EMAIL)
      .nextAttemptAt(nextAttemptAt)
      .attempts(0)
      .build();
  }

  private OutboxMessage createMessageWithChannel(
    Instant nextAttemptAt,
    Channel channel
  ) {
    return OutboxMessage
      .builder()
      .id(UUID.randomUUID())
      .recipient("test@example.com")
      .subject("Test Subject")
      .body("Test Body")
      .channel(channel)
      .nextAttemptAt(nextAttemptAt)
      .attempts(0)
      .build();
  }
}
