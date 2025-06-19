/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.service;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialValidationService {

  private final PasswordEncoder passwordEncoder;

  public void validateCredentials(User user, String password) {
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      log.warn("Invalid password attempt for user: {}", user.getEmail());
      throw AuthException.badCredentials();
    }
  }

  public void validateUserEnabled(User user) {
    if (!user.isEnabled()) {
      log.warn("Account not verified for user: {}", user.getEmail());
      throw AuthException.accountNotVerified();
    }
  }
}
