/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.service;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenDomainService {

  private final VerificationTokenRepository verificationTokenRepository;

  public VerificationToken findValidToken(UUID token) {
    VerificationToken verificationToken = verificationTokenRepository
      .findByTokenAndUsedFalse(token)
      .orElseThrow(() -> {
        log.warn("Token de verificação inválido: {}", token);
        return AuthException.verificationInvalid();
      });

    validateTokenNotExpired(verificationToken, token);
    return verificationToken;
  }

  public void markTokenAsUsed(VerificationToken token) {
    token.setUsed(true);
    verificationTokenRepository.save(token);
  }

  private void validateTokenNotExpired(
    VerificationToken token,
    UUID tokenValue
  ) {
    if (token.getExpiresAt().isBefore(Instant.now())) {
      log.error("Token expirado: {}", tokenValue);
      throw AuthException.verificationExpired();
    }
  }
}
