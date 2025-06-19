/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.domain.event.PasswordResetRequestedEvent;
import com.kleberrhuan.houer.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetNotificationService {

  private final ApplicationEventPublisher eventPublisher;

  public void publishPasswordResetEvent(User user, String rawToken) {
    PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
      user.getEmail(),
      user.getName(),
      rawToken
    );

    eventPublisher.publishEvent(event);
    log.debug("Password reset event published for user: {}", user.getEmail());
  }

  public void logIgnoredRequest() {
    log.info("Password reset requested for non-existent email - ignored");
  }
}
