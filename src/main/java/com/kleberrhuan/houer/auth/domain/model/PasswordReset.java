/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import com.kleberrhuan.houer.common.infra.persistence.Auditable;
import com.kleberrhuan.houer.user.domain.model.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "password_reset", schema = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset extends Auditable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, length = 255)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  public boolean isUsed() {
    return usedAt != null;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !isUsed() && !isExpired();
  }

  public void markAsUsed() {
    this.usedAt = Instant.now();
  }
}
