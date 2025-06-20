/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.application.factory.VerificationTokenFactory;
import com.kleberrhuan.houer.auth.domain.model.VerificationToken;
import com.kleberrhuan.houer.auth.domain.repository.VerificationTokenRepository;
import com.kleberrhuan.houer.auth.domain.service.VerificationTokenDomainService;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.user.application.mapper.UserMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.domain.repository.UserRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

  private final UserRepository userRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final VerificationTokenFactory tokenFactory;
  private final VerificationTokenDomainService tokenDomainService;
  private final UserVerificationService userVerificationService;
  private final NotificationService notificationService;

  @Counted(
    value = "auth.registration.executions",
    description = "Execuções de registro de usuário"
  )
  @Timed(
    value = "auth.registration.time",
    description = "Tempo de registro de usuário"
  )
  @Transactional
  public void register(RegisterRequest request, String baseUrl) {
    User user = createAndSaveUser(request);
    VerificationToken token = createAndSaveVerificationToken(user);
    sendVerificationNotification(user, baseUrl, token);

    log.info("Novo usuário registrado: {}", user.getEmail());
  }

  @Counted(
    value = "auth.verification.executions",
    description = "Execuções de verificação de token"
  )
  @Timed(
    value = "auth.verification.time",
    description = "Tempo de verificação de token"
  )
  @Transactional
  public void verify(UUID token) {
    VerificationToken verificationToken = tokenDomainService.findValidToken(
      token
    );
    User user = userVerificationService.findUserForVerification(
      verificationToken.getUserId(),
      token
    );

    if (!user.isEnabled()) {
      userVerificationService.enableUser(user, token);
      tokenDomainService.markTokenAsUsed(verificationToken);
    }
  }

  @Counted(
    value = "auth.verification.resend.executions",
    description = "Execuções de reenvio de verificação"
  )
  @Timed(
    value = "auth.verification.resend.time",
    description = "Tempo de reenvio de verificação"
  )
  @Transactional
  public void resendVerification(String email, String baseUrl) {
    User user = userRepository
      .findByEmailIgnoreCaseAll(email)
      .orElseThrow(() -> {
        log.warn("Tentativa de reenvio para email inexistente: {}", email);
        return new EntityNotFoundException("Email", email);
      });

    if (user.isEnabled()) {
      log.info("Tentativa de reenvio para conta já verificada: {}", email);
      return;
    }

    verificationTokenRepository.deleteByUserIdAndUsedFalse(user.getId());
    VerificationToken newToken = createAndSaveVerificationToken(user);

    sendVerificationNotification(user, baseUrl, newToken);

    log.info("Email de verificação reenviado para: {}", email);
  }

  private User createAndSaveUser(RegisterRequest request) {
    User user = userMapper.toEntity(request, passwordEncoder);
    return userRepository.save(user);
  }

  private VerificationToken createAndSaveVerificationToken(User user) {
    VerificationToken token = tokenFactory.createForUser(user.getId());
    return verificationTokenRepository.save(token);
  }

  private void sendVerificationNotification(
    User user,
    String baseUrl,
    VerificationToken token
  ) {
    notificationService.publishUserRegisteredEvent(
      user.getEmail(),
      user.getName(),
      baseUrl,
      token.getToken()
    );
  }
}
