/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.listener;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.common.application.dispatcher.notification.NotificationDispatcher;
import com.kleberrhuan.houer.common.application.service.notification.MailTemplateService;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import io.micrometer.core.annotation.Counted;
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
public class VerificationMailListener {

  private final MailTemplateService templating;
  private final NotificationDispatcher dispatcher;

  @Async("mailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Counted(value = "email.sent.verify")
  public void handle(UserRegisteredEvent evt) {
    String html = templating.render(
      "verify-account",
      Map.of("name", evt.name(), "verifyLink", evt.verifyLink())
    );

    NotificationModel mail = new NotificationModel(
      Channel.EMAIL,
      evt.email(),
      "Confirme sua conta",
      html
    );

    dispatcher.dispatch(mail);
    log.info("Verification email sent to: {}", evt.email());
  }
}
