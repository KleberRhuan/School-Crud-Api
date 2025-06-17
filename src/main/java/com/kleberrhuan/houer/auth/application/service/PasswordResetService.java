/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.mapper.PasswordResetMapper;
import com.kleberrhuan.houer.auth.domain.event.PasswordResetRequestedEvent;
import com.kleberrhuan.houer.auth.domain.exception.AuthException;
import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import com.kleberrhuan.houer.auth.domain.repository.PasswordResetRepository;
import com.kleberrhuan.houer.auth.infra.properties.AuthProperties;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ForgotPasswordRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ResetPasswordRequest;
import com.kleberrhuan.houer.common.application.service.HashService;
import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

  private final PasswordResetRepository passwordResetRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;
  private final AuthProperties authProperties;
  private final HashService hashService;
  private final PasswordResetMapper mapper;

  @Timed("auth.password_reset.request")
  @Counted(
    value = "auth.password_reset.requested",
    extraTags = { "result", "total" }
  )
  @Transactional
  public void requestReset(ForgotPasswordRequest request) {
    User user = userRepository
      .findByEmailIgnoreCase(request.email())
      .orElseThrow(() -> new EntityNotFoundException("Usuário", request.email())
      );

    String rawToken = hashService.generateToken();
    String tokenHash = hashService.hash(rawToken);
    Instant expiresAt = Instant.now().plus(authProperties.getReset().getTtl());

    passwordResetRepository.save(mapper.toEntity(user, tokenHash, expiresAt));

    eventPublisher.publishEvent(
      new PasswordResetRequestedEvent(user.getEmail(), user.getName(), rawToken)
    );

    log.info("Password reset requested for non-existent email - ignored");
  }

  @Timed("auth.password_reset.reset")
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    String tokenHash = hashService.hash(request.token());

    PasswordReset passwordReset = passwordResetRepository
      .findValid(tokenHash, Instant.now())
      .orElseThrow(AuthException::passwordResetTokenInvalid);

    passwordReset.markAsUsed();

    User user = passwordReset.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

    log.info("Password successfully reset for user ID: {}", user.getId());
  }

  @Timed("auth.password_reset.validation")
  @Transactional(readOnly = true)
  public boolean isTokenValid(String token) {
    return passwordResetRepository
      .findValid(hashService.hash(token), Instant.now())
      .isPresent();
  }
}
