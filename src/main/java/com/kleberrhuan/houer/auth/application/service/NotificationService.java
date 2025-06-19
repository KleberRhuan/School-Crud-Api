/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.service;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final ApplicationEventPublisher eventPublisher;

  @Value("${app.verification.url-path:/api/v1/auth/verify}")
  private String verificationUrlPath;

  public void publishUserRegisteredEvent(
    String email,
    String name,
    String baseUrl,
    UUID token
  ) {
    String verificationUrl = buildVerificationUrl(baseUrl, token);

    UserRegisteredEvent event = new UserRegisteredEvent(
      email,
      name,
      verificationUrl
    );
    eventPublisher.publishEvent(event);
  }

  private String buildVerificationUrl(String baseUrl, UUID token) {
    return baseUrl + verificationUrlPath + "?token=" + token;
  }
}
