/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.notification;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;

public interface OutboxProcessor<T extends OutboxMessage> {
  boolean process(T message) throws RuntimeException;

  void nack(OutboxMessage msg, OutboxStore store);
}
