/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.factory;

import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationTokenFactory {

  @Value("${app.verification.token.expiry-hours:24}")
  private int expiryHours;

  public VerificationToken createForUser(Long userId) {
    UUID token = UUID.randomUUID();
    Instant expiresAt = Instant.now().plus(Duration.ofHours(expiryHours));

    return new VerificationToken(token, userId, expiresAt, false);
  }
}
