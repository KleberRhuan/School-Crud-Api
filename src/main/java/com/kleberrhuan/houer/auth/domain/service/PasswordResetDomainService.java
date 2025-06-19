/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.service;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import com.kleberrhuan.houer.auth.domain.repository.PasswordResetRepository;
import com.kleberrhuan.houer.common.application.service.HashService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetDomainService {

  private final PasswordResetRepository passwordResetRepository;
  private final HashService hashService;

  public PasswordReset findValidPasswordReset(String token) {
    String tokenHash = hashService.hash(token);

    return passwordResetRepository
      .findValid(tokenHash, Instant.now())
      .orElseThrow(() -> {
        log.warn("Token de reset de senha inválido ou expirado");
        return AuthException.passwordResetTokenInvalid();
      });
  }

  public boolean isTokenValid(String token) {
    String tokenHash = hashService.hash(token);
    return passwordResetRepository
      .findValid(tokenHash, Instant.now())
      .isPresent();
  }

  public void markPasswordResetAsUsed(PasswordReset passwordReset) {
    passwordReset.markAsUsed();
    passwordResetRepository.save(passwordReset);
  }
}
