/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.listener;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.common.application.dispatcher.notification.NotificationDispatcher;
import com.kleberrhuan.houer.common.application.service.notification.MailTemplateService;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VerificationMailListener {

  private final MailTemplateService templating;
  private final NotificationDispatcher dispatcher;
  private final MeterRegistry meter;

  @Async("mailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(UserRegisteredEvent evt) {
    String html = templating.render(
      "verify-account",
      Map.of("name", evt.name(), "verifyLink", evt.verifyLink())
    );

    NotificationModel mail = NotificationModel
      .builder()
      .channel(Channel.EMAIL)
      .to(evt.email())
      .subject("Confirme sua conta")
      .message(html)
      .build();

    dispatcher.dispatch(mail);
    meter.counter("email.sent.verify").increment();
  }
}
