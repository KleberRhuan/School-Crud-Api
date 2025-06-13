/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.notification;

import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import java.util.Optional;
import java.util.UUID;

public interface OutboxStore {
  void save(OutboxMessage msg);

  Optional<OutboxMessage> pollNextDue();

  void delete(UUID id);

  StoreHealth health();

  enum StoreHealth {
    UP,
    DOWN,
  }
}
