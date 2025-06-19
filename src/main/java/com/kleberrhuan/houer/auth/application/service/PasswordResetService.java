/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.factory.PasswordResetFactory;
import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import com.kleberrhuan.houer.auth.domain.repository.PasswordResetRepository;
import com.kleberrhuan.houer.auth.domain.service.PasswordResetDomainService;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ForgotPasswordRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ResetPasswordRequest;
import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

  private final PasswordResetRepository passwordResetRepository;
  private final UserRepository userRepository;
  private final PasswordResetFactory passwordResetFactory;
  private final PasswordResetDomainService passwordResetDomainService;
  private final PasswordUpdateService passwordUpdateService;
  private final PasswordResetNotificationService notificationService;

  @Timed("auth.password_reset.request")
  @Counted(
    value = "auth.password_reset.requested",
    extraTags = { "result", "total" }
  )
  @Transactional
  public void requestReset(ForgotPasswordRequest request) {
    User user = findUserByEmail(request.email());
    PasswordResetFactory.PasswordResetTokenData tokenData =
      passwordResetFactory.createForUser(user);

    passwordResetRepository.save(tokenData.passwordReset());
    notificationService.publishPasswordResetEvent(user, tokenData.rawToken());
    notificationService.logIgnoredRequest();
  }

  @Timed("auth.password_reset.reset")
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    PasswordReset passwordReset =
      passwordResetDomainService.findValidPasswordReset(request.token());
    passwordResetDomainService.markPasswordResetAsUsed(passwordReset);

    User user = passwordReset.getUser();
    passwordUpdateService.updateUserPassword(user, request.newPassword());
  }

  @Timed("auth.password_reset.validation")
  @Transactional(readOnly = true)
  public boolean isTokenValid(String token) {
    return passwordResetDomainService.isTokenValid(token);
  }

  private User findUserByEmail(String email) {
    return userRepository
      .findByEmailIgnoreCase(email)
      .orElseThrow(() -> new EntityNotFoundException("Usuário", email));
  }
}
