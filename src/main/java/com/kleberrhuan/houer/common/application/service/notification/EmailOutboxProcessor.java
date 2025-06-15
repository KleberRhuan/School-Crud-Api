/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import com.kleberrhuan.houer.common.application.port.notification.EmailNotification;
import com.kleberrhuan.houer.common.application.port.notification.OutboxProcessor;
import com.kleberrhuan.houer.common.application.port.notification.OutboxStore;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOutboxProcessor implements OutboxProcessor<OutboxMessage> {

  private final EmailNotification mail;

  @Override
  public boolean process(OutboxMessage msg) {
    mail.send(msg.toNotification());
    return true;
  }

  @Override
  public void nack(OutboxMessage msg, OutboxStore store) {
    msg.fail();
    store.save(msg);
  }
}
