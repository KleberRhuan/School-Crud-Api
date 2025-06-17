/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.listener;

import com.kleberrhuan.houer.auth.domain.event.PasswordResetRequestedEvent;
import com.kleberrhuan.houer.auth.infra.properties.AuthProperties;
import com.kleberrhuan.houer.common.application.dispatcher.notification.NotificationDispatcher;
import com.kleberrhuan.houer.common.application.service.notification.MailTemplateService;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetMailListener {

  private final MailTemplateService templating;
  private final NotificationDispatcher dispatcher;
  private final AuthProperties authProperties;

  @Async("mailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Observed(
    name = "email.sent.reset",
    contextualName = "passwordResetMail",
    lowCardinalityKeyValues = { "operation", "reset" }
  )
  public void handle(PasswordResetRequestedEvent evt) {
    String resetLink = String.format(
      "%s/reset-password?token=%s",
      authProperties.getReset().getFrontendBaseUrl(),
      evt.rawToken()
    );

    String html = templating.render(
      "reset-password",
      Map.of(
        "name",
        evt.name(),
        "resetLink",
        resetLink,
        "ttlMinutes",
        authProperties.getReset().getTtl().toMinutes()
      )
    );

    NotificationModel mail = new NotificationModel(
      Channel.EMAIL,
      evt.email(),
      "Redefinição de senha",
      html
    );

    dispatcher.dispatch(mail);
    log.info("Password reset email sent to: {}", evt.email());
  }
}
