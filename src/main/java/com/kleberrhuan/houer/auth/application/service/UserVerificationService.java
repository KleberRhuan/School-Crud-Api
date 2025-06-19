/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerificationService {

  private final UserRepository userRepository;

  public User findUserForVerification(Long userId, UUID token) {
    User user = userRepository
      .findByIdIgnoreEnabled(userId)
      .orElseThrow(() -> {
        log.error("Usuário não encontrado para verificação: id={}", userId);
        return AuthException.verificationInvalid();
      });

    if (user.isEnabled()) {
      log.info("Usuário já verificado: token={}", token);
      return user;
    }

    return user;
  }

  public void enableUser(User user, UUID token) {
    if (!user.isEnabled()) {
      user.setEnabled(true);
      userRepository.save(user);
      log.info("Verificação de usuário bem-sucedida: token={}", token);
    }
  }
}
