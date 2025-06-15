/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.repository;

import com.kleberrhuan.houer.auth.domain.model.RefreshToken;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository
  extends JpaRepository<RefreshToken, UUID> {
  @Modifying
  @Query("delete from RefreshToken r where r.expiresAt < :now")
  void deleteExpired(Instant now);
}
