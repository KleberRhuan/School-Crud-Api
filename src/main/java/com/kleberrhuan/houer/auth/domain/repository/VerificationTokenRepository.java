/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.repository;

import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository
  extends JpaRepository<VerificationToken, UUID> {
  Optional<VerificationToken> findByTokenAndUsedFalse(UUID token);

  @Modifying
  @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now")
  long deleteByExpiresAtBefore(@Param("now") Instant now);
}
