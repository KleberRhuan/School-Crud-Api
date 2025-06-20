/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.model;

import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "notification_outbox", schema = "config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String recipient;

  private String subject;

  @Lob
  private String body;

  @Enumerated(EnumType.STRING)
  private Channel channel;

  private Instant nextAttemptAt;

  private int attempts;

  public static OutboxMessage create(NotificationModel n) {
    return OutboxMessage
      .builder()
      .recipient(n.to())
      .subject(n.subject())
      .body(n.message())
      .channel(n.channel())
      .attempts(0)
      .nextAttemptAt(Instant.now().plus(Duration.ofSeconds(30))) // Reduz de 10min para 30s
      .build();
  }

  public NotificationModel toNotification() {
    return new NotificationModel(channel, recipient, subject, body);
  }

  public void fail() {
    this.attempts++;
    long delay = Math.min(60, (int) Math.pow(2, attempts));
    Instant candidate = nextAttemptAt.plus(Duration.ofMinutes(delay));
    if (candidate.isBefore(nextAttemptAt)) {
      candidate = nextAttemptAt;
    }
    this.nextAttemptAt = candidate;
  }
}
