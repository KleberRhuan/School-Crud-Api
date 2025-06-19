/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.job;

import com.kleberrhuan.houer.auth.domain.repository.PasswordResetRepository;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
  value = "app.cleanup.enabled",
  havingValue = "true",
  matchIfMissing = true
)
public class TokenCleanupJob {

  private final PasswordResetRepository passwordResetRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final VerificationTokenRepository verificationTokenRepository;

  @Scheduled(cron = "${app.cleanup.cron:0 0 * * * *}")
  @Transactional
  @Timed(
    value = "auth.cleanup.time",
    description = "Tempo de execução da limpeza de tokens"
  )
  @Counted(
    value = "auth.cleanup.executions",
    description = "Execuções da limpeza de tokens"
  )
  public void cleanupExpiredTokens() {
    Instant now = Instant.now();
    long startTime = System.currentTimeMillis();

    log.info("Iniciando limpeza de tokens expirados em {}", now);

    try {
      long deletedPasswordResets = cleanupPasswordResetTokens(now);
      long deletedRefreshTokens = cleanupRefreshTokens(now);
      long deletedVerificationTokens = cleanupVerificationTokens(now);

      long totalDeleted =
        deletedPasswordResets +
        deletedRefreshTokens +
        deletedVerificationTokens;
      long executionTime = System.currentTimeMillis() - startTime;

      log.info(
        "Limpeza concluída em {}ms - Total removido: {} tokens " +
        "(Password Resets: {}, Refresh Tokens: {}, Verification Tokens: {})",
        executionTime,
        totalDeleted,
        deletedPasswordResets,
        deletedRefreshTokens,
        deletedVerificationTokens
      );
    } catch (Exception e) {
      log.error("Erro durante a limpeza de tokens expirados", e);
      throw e;
    }
  }

  private long cleanupPasswordResetTokens(Instant now) {
    try {
      long countBefore = passwordResetRepository.count();
      passwordResetRepository.deleteExpired(now);
      passwordResetRepository.flush();
      long countAfter = passwordResetRepository.count();

      long deleted = countBefore - countAfter;
      log.debug("Tokens de reset de senha removidos: {}", deleted);
      return deleted;
    } catch (Exception e) {
      log.error("Erro ao limpar tokens de reset de senha", e);
      return 0;
    }
  }

  private long cleanupRefreshTokens(Instant now) {
    try {
      long countBefore = refreshTokenRepository.count();
      refreshTokenRepository.deleteExpired(now);
      refreshTokenRepository.flush();
      long countAfter = refreshTokenRepository.count();

      long deleted = countBefore - countAfter;
      log.debug("Refresh tokens removidos: {}", deleted);
      return deleted;
    } catch (Exception e) {
      log.error("Erro ao limpar refresh tokens", e);
      return 0;
    }
  }

  private long cleanupVerificationTokens(Instant now) {
    try {
      long deleted = verificationTokenRepository.deleteByExpiresAtBefore(now);
      verificationTokenRepository.flush();
      log.debug("Tokens de verificação removidos: {}", deleted);
      return deleted;
    } catch (Exception e) {
      log.error("Erro ao limpar tokens de verificação", e);
      return 0;
    }
  }
}
