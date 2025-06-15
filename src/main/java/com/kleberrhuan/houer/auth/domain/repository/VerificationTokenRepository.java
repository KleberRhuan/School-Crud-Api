/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.repository;

import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository
  extends JpaRepository<VerificationToken, UUID> {
  Optional<VerificationToken> findByTokenAndUsedFalse(UUID token);
}
