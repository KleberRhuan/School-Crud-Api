/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.domain.repository.PasswordResetRepository;
import com.kleberrhuan.houer.auth.domain.repository.RefreshTokenRepository;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenCleanupJob")
class TokenCleanupJobTest {

  @Mock
  private PasswordResetRepository passwordResetRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private VerificationTokenRepository verificationTokenRepository;

  private TokenCleanupJob tokenCleanupJob;

  @BeforeEach
  void setUp() {
    tokenCleanupJob =
      new TokenCleanupJob(
        passwordResetRepository,
        refreshTokenRepository,
        verificationTokenRepository
      );
  }

  @Test
  @DisplayName("deve executar limpeza de todos os tipos de tokens")
  void shouldCleanupAllTokenTypes() {
    // Arrange
    when(passwordResetRepository.count())
      .thenReturn(100L) // antes
      .thenReturn(80L); // depois

    when(refreshTokenRepository.count())
      .thenReturn(50L) // antes
      .thenReturn(30L); // depois

    when(
      verificationTokenRepository.deleteByExpiresAtBefore(any(Instant.class))
    )
      .thenReturn(15L);

    // Act
    tokenCleanupJob.cleanupExpiredTokens();

    // Assert
    verify(passwordResetRepository).deleteExpired(any(Instant.class));
    verify(passwordResetRepository).flush();
    verify(refreshTokenRepository).deleteExpired(any(Instant.class));
    verify(refreshTokenRepository).flush();
    verify(verificationTokenRepository)
      .deleteByExpiresAtBefore(any(Instant.class));
    verify(verificationTokenRepository).flush();
  }

  @Test
  @DisplayName("deve continuar execução mesmo se um repositório falhar")
  void shouldContinueExecutionEvenIfOneRepositoryFails() {
    // Arrange
    when(passwordResetRepository.count())
      .thenThrow(new RuntimeException("Database error"));

    when(refreshTokenRepository.count()).thenReturn(50L).thenReturn(30L);

    when(
      verificationTokenRepository.deleteByExpiresAtBefore(any(Instant.class))
    )
      .thenReturn(15L);

    // Act
    tokenCleanupJob.cleanupExpiredTokens();

    // Assert - deve continuar executando outros repositórios mesmo com falha
    verify(refreshTokenRepository).deleteExpired(any(Instant.class));
    verify(verificationTokenRepository)
      .deleteByExpiresAtBefore(any(Instant.class));
  }

  @Test
  @DisplayName("deve fazer flush após cada operação de limpeza")
  void shouldFlushAfterEachCleanupOperation() {
    // Arrange
    when(passwordResetRepository.count()).thenReturn(10L, 8L);
    when(refreshTokenRepository.count()).thenReturn(5L, 3L);
    when(
      verificationTokenRepository.deleteByExpiresAtBefore(any(Instant.class))
    )
      .thenReturn(2L);

    // Act
    tokenCleanupJob.cleanupExpiredTokens();

    // Assert
    verify(passwordResetRepository).flush();
    verify(refreshTokenRepository).flush();
    verify(verificationTokenRepository).flush();
  }
}
