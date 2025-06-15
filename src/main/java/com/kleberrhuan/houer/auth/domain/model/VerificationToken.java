/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "verification_tokens", schema = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

  @Id
  UUID token;

  Long userId;
  Instant expiresAt;
  boolean used;
}
