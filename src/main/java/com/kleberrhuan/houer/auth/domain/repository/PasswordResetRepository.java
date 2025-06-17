/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.repository;

import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetRepository
  extends JpaRepository<PasswordReset, UUID> {
  @Query(
    """
      SELECT pr FROM PasswordReset pr
      WHERE pr.tokenHash = :tokenHash
        AND pr.usedAt IS NULL
        AND pr.expiresAt > :now
      """
  )
  Optional<PasswordReset> findValid(
    @Param("tokenHash") String tokenHash,
    @Param("now") Instant now
  );

  @Query("DELETE FROM PasswordReset pr WHERE pr.expiresAt < :now")
  void deleteExpired(@Param("now") Instant now);

  @Query(
    """
      SELECT COUNT(pr) FROM PasswordReset pr
      WHERE pr.user.id = :userId
        AND pr.usedAt IS NULL
        AND pr.expiresAt > :now
      """
  )
  long countActiveByUserId(
    @Param("userId") Long userId,
    @Param("now") Instant now
  );
}
