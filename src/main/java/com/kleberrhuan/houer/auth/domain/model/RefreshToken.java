/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens", schema = "account")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

  @Id
  UUID series;

  Long userId;
  Instant expiresAt;
  boolean used;

  public void use() {
    this.used = true;
  }
}
