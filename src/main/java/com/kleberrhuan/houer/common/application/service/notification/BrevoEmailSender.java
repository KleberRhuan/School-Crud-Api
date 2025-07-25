/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.mapper.BrevoMapper;
import com.kleberrhuan.houer.common.application.port.notification.EmailNotification;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.infra.adapter.notification.client.brevo.BrevoApi;
import com.kleberrhuan.houer.common.infra.exception.EmailDeliveryException;
import com.kleberrhuan.houer.common.infra.outbox.ResilientOutboxStore;
import com.kleberrhuan.houer.common.infra.properties.BrevoProps;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.SendSmtpEmail;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.response.SendSmtpEmailResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
@Service
@Primary
@Slf4j
public class BrevoEmailSender implements EmailNotification {

  private final BrevoMapper mapper;
  private final BrevoProps props;
  private final ResilientOutboxStore store;
  private final BrevoApi api;

  @Override
  public Provider provider() {
    return new Provider(Channel.EMAIL, "brevo");
  }

  @Override
  @Timed(value = "email.brevo.duration", extraTags = { "provider", "brevo" })
  @Retry(name = "brevo-email")
  @CircuitBreaker(name = "brevo-email", fallbackMethod = "fallback")
  @Observed(name = "email.brevo.send")
  public void send(@Valid NotificationModel n) {
    try {
      SendSmtpEmail payload = mapper.toBrevo(n, props);
      SendSmtpEmailResponse resp = this.send(payload, provider());
      log.info("Email sent via brevo: messageId={}", resp.messageId());
    } catch (EmailDeliveryException e) {
      log.warn("Email delivery failed for {}: {}", n.to(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.warn(
        "Email service error for {} (will retry): {}",
        n.to(),
        e.getMessage()
      );
      throw new RuntimeException("Temporary email service error", e);
    }
  }

  @Counted(value = "email.outbox.total", extraTags = { "provider", "brevo" })
  public void fallback(NotificationModel n, Throwable ex) {
    log.warn(
      "Email fallback triggered for {}: {} - Routing to outbox",
      n.to(),
      ex.getMessage()
    );

    try {
      store.save(OutboxMessage.create(n));
      log.info(
        "Email routed to outbox: to={} subject='{}'",
        n.to(),
        n.subject()
      );
    } catch (Exception outboxEx) {
      log.error(
        "Failed to save email to outbox: to={} subject='{}' error={}",
        n.to(),
        n.subject(),
        outboxEx.getMessage()
      );
      throw new RuntimeException("Failed to route email to outbox", outboxEx);
    }
  }

  public SendSmtpEmailResponse send(SendSmtpEmail m, Provider p) {
    try {
      return api.sendSimpleEmail(m);
    } catch (Exception e) {
      throw new EmailDeliveryException(p);
    }
  }
}
